module Main exposing (..)

import Browser exposing (Document, UrlRequest(..))
import Browser.Navigation as Nav
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Json.Decode as Decode exposing (Decoder, field, string)
import RemoteData exposing (..)
import Table exposing (defaultCustomizations)
import Url exposing (Url)
import Url.Builder as UrlBuilder



-- MAIN


main =
    Browser.application
        { init = init
        , update = update
        , subscriptions = subscriptions
        , view = view
        , onUrlRequest = LinkClicked
        , onUrlChange = UrlChanged
        }



-- MODEL


type alias Model =
    { navKey : Nav.Key
    , url : Url
    , pdfs : WebData Pdfs
    , tableState : Table.State
    , query : String
    , currentPdf : Maybe PdfRecord
    }


type alias Pdfs =
    List PdfRecord


type alias PdfRecord =
    { name : String
    , path : String
    }


init : () -> Url.Url -> Nav.Key -> ( Model, Cmd Msg )
init _ url navKey =
    ( { navKey = navKey, url = url, pdfs = Loading, tableState = Table.initialSort "Name", query = "", currentPdf = Nothing }, getPdfs )



-- UPDATE


type Msg
    = LinkClicked UrlRequest
    | UrlChanged Url
    | GotPdfs (WebData Pdfs)
    | LoadPdf PdfRecord
    | Loaded (WebData ())
    | SetQuery String
    | SetTableState Table.State
    | NextPage
    | PrevPage


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        LinkClicked (Internal url) ->
            ( model, Nav.pushUrl model.navKey (Url.toString url) )

        LinkClicked (External url) ->
            ( model, Nav.load url )

        UrlChanged url ->
            let
                load =
                    Maybe.map (\pdf -> loadPdf pdf.path) model.currentPdf
            in
            if url.path == "/upDown" then
                ( model, Maybe.withDefault Cmd.none load )

            else
                ( { model | currentPdf = Nothing }, Cmd.none )

        GotPdfs response ->
            ( { model | pdfs = response }, Cmd.none )

        LoadPdf pdf ->
            let
                url =
                    model.url

                upDownUrl =
                    { url | path = "/upDown", query = Just (urlFormEncodedPdfPath pdf.path) }
            in
            ( { model | currentPdf = Just pdf }, Nav.pushUrl model.navKey (Url.toString upDownUrl) )

        Loaded _ ->
            ( model, Cmd.none )

        SetQuery newQuery ->
            ( { model | query = newQuery }, Cmd.none )

        SetTableState newState ->
            ( { model | tableState = newState }, Cmd.none )

        PrevPage ->
            ( model, nextPage )

        NextPage ->
            ( model, prevPage )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.none



-- VIEW


view : Model -> Document Msg
view model =
    case model.currentPdf of
        Nothing ->
            { title = "PDF viewer", body = [ viewTablePage model ] }

        Just currentPdf ->
            { title = currentPdf.name, body = [ viewUpDownPage currentPdf ] }


viewUpDownPage : PdfRecord -> Html Msg
viewUpDownPage currentPdf =
    section [ class "section" ]
        [ div [ class "container" ]
            [ h1 [ class "title" ] [ text currentPdf.name ]
            , p [ class "buttons" ]
                [ button [ class "button is-large", onClick NextPage ] [ span [ class "icon is-large" ] [ i [ class "fas fa-chevron-left fa-2x" ] [] ] ]
                , button [ class "button is-large", onClick PrevPage ] [ span [ class "icon is-large" ] [ i [ class "fas fa-chevron-right fa-2x" ] [] ] ]
                ]
            ]
        ]


viewTablePage : Model -> Html Msg
viewTablePage model =
    section [ class "section" ]
        [ div [ class "container" ]
            [ h1 [ class "title" ] [ text "Pdfs" ]
            , input [ class "input", placeholder "Search by Name", onInput SetQuery ] []
            , viewPdfs model
            ]
        ]


viewPdfs : Model -> Html Msg
viewPdfs model =
    case model.pdfs of
        NotAsked ->
            text "Huh?"

        Failure err ->
            text "Failed to get the pdfs."

        Loading ->
            text "Loading pdfs..."

        Success pdfs ->
            let
                lowerQuery =
                    String.toLower model.query

                filteredPdfs =
                    List.filter (String.contains lowerQuery << String.toLower << .name) pdfs
            in
            Table.view config model.tableState filteredPdfs


config : Table.Config PdfRecord Msg
config =
    Table.customConfig
        { toId = .name
        , toMsg = SetTableState
        , columns =
            [ Table.stringColumn "Name" .name ]
        , customizations =
            { defaultCustomizations | tableAttrs = tableAttrs, rowAttrs = loadPdfRowAttrs }
        }


loadPdfRowAttrs : PdfRecord -> List (Attribute Msg)
loadPdfRowAttrs pdf =
    [ onClick (LoadPdf pdf) ]


tableAttrs : List (Attribute Msg)
tableAttrs =
    [ class "table is-striped is-fullwidth is-hoverable", style "table-layout" "fixed" ]



-- HTTP


getPdfs : Cmd Msg
getPdfs =
    Http.get
        { url = "/api/pdfs"
        , expect = Http.expectJson (RemoteData.fromResult >> GotPdfs) recordsDecoder
        }


recordsDecoder : Decoder Pdfs
recordsDecoder =
    Decode.list pdfRecordDecoder


pdfRecordDecoder : Decoder PdfRecord
pdfRecordDecoder =
    Decode.map2 PdfRecord
        (field "name" Decode.string)
        (field "path" Decode.string)


loadPdf : String -> Cmd Msg
loadPdf path =
    Http.post
        { url = "/api/viewer/load"
        , expect = Http.expectWhatever (RemoteData.fromResult >> Loaded)
        , body = Http.stringBody "application/x-www-form-urlencoded" (urlFormEncodedPdfPath path)
        }



-- Have to remove the ? since it appears twice


urlFormEncodedPdfPath : String -> String
urlFormEncodedPdfPath path =
    String.dropLeft 1 (UrlBuilder.toQuery [ UrlBuilder.string "pdf" path ])


nextPage : Cmd Msg
nextPage =
    Http.post
        { url = "/api/viewer/next"
        , expect = Http.expectWhatever (RemoteData.fromResult >> Loaded)
        , body = Http.emptyBody
        }


prevPage : Cmd Msg
prevPage =
    Http.post
        { url = "/api/viewer/prev"
        , expect = Http.expectWhatever (RemoteData.fromResult >> Loaded)
        , body = Http.emptyBody
        }
