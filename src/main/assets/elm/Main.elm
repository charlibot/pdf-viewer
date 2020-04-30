module Main exposing (..)

import Browser exposing (Document, UrlRequest(..))
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Json.Decode as Decode exposing (Decoder, field, string)
import RemoteData exposing (..)
import Url.Builder as UrlBuilder
import Table exposing (defaultCustomizations)
import Url exposing (Url)
import Browser.Navigation as Nav

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
type alias ModelM =
    { navKey : Nav.Key
    , url : Url
    , pdfs : WebData Pdfs
    , tableState : Table.State
    , query : String
    , currentPdf : Maybe PdfRecord
    }


type alias Model =
    { navKey : Nav.Key
    , url : Url
    , page : Page
    }

type alias TablePageModel =
    { pdfs : WebData Pdfs
    , tableState : Table.State
    , query : String
    }

type alias UpDownPageModel =
    { currentPdf : PdfRecord }

type Page = TablePage TablePageModel | UpDownPage UpDownPageModel

type alias Pdfs = List PdfRecord
type alias PdfRecord =
    {  name : String
    ,  path : String
    }

init : () -> Url.Url -> Nav.Key -> ( Model, Cmd Msg )
init _ url navKey =
  let
      tablePage = TablePage {pdfs = Loading, tableState = Table.initialSort "Name", query = ""}
  in
  ({ navKey = navKey, url = url, page = tablePage }, getPdfs)

-- UPDATE
type Msg
  = LinkClicked UrlRequest
  | UrlChanged Url
  | GotPdfs (WebData Pdfs)
  | LoadPdf PdfRecord
  | Loaded (WebData ())
  | SetQuery String
  | SetTableState Table.State

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    LinkClicked (Internal url) ->
      ( model, Nav.pushUrl model.navKey (Url.toString url) )

    LinkClicked (External url) ->
      ( model, Nav.load url )

    UrlChanged url ->
      ( { model | url = url }, Cmd.none )

    GotPdfs response ->
      (updateTablePage (\m -> { m | pdfs = response }) model, Cmd.none)

    LoadPdf pdf ->
        let
            url = model.url
            upDownUrl = { url | path = "/upDown" }
            commands = Cmd.batch [getPdf pdf.path, Nav.pushUrl model.navKey (Url.toString upDownUrl)]
        in
            ({ model | page = UpDownPage { currentPdf = pdf }}, commands)

    Loaded _ ->
        (model, Cmd.none)

    SetQuery newQuery ->
      (updateTablePage (\m -> { m | query = newQuery }) model, Cmd.none)

    SetTableState newState ->
      (updateTablePage (\m -> { m | tableState = newState }) model, Cmd.none)

updateTablePage : (TablePageModel -> TablePageModel) -> Model -> Model
updateTablePage updateFunc model =
    let
      updatedPage = case model.page of
          TablePage m -> TablePage (updateFunc m)
          UpDownPage m -> UpDownPage m
    in
        { model | page = updatedPage}


-- SUBSCRIPTIONS
subscriptions : Model -> Sub Msg
subscriptions _ =
  Sub.none

-- VIEW
view : Model -> Document Msg
view model =
    case model.page of
        TablePage m -> { title = "PDF viewer", body = [viewTablePage m]}
        UpDownPage m -> { title = m.currentPdf.name, body = [viewUpDownPage m]}

viewUpDownPage : UpDownPageModel -> Html Msg
viewUpDownPage model =
    section [ class "section" ]
        [ div [ class "container" ]
            [ h1 [ class "title" ] [ text model.currentPdf.name ]
            ]
        ]

viewTablePage : TablePageModel -> Html Msg
viewTablePage model =
  section [ class "section" ]
    [ div [ class "container" ]
        [ h1 [ class "title" ] [ text "Pdfs" ]
        , input [ class "input", placeholder "Search by Name", onInput SetQuery ] []
        , viewPdfs model
        ]
    ]

viewPdfs : TablePageModel -> Html Msg
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
        [ Table.stringColumn "Name" .name]
    , customizations =
        { defaultCustomizations | tableAttrs = tableAttrs, rowAttrs = loadPdf }
    }

loadPdf : PdfRecord -> List (Attribute Msg)
loadPdf pdf = [ onClick (LoadPdf pdf) ]

tableAttrs : List (Attribute Msg)
tableAttrs = [ class "table is-striped is-fullwidth is-hoverable", style "table-layout" "fixed" ]

-- HTTP
getPdfs : Cmd Msg
getPdfs =
  Http.get
    { url = "/api/pdfs"
    , expect  = Http.expectJson (RemoteData.fromResult >> GotPdfs) recordsDecoder
    }

recordsDecoder : Decoder Pdfs
recordsDecoder = Decode.list pdfRecordDecoder

pdfRecordDecoder : Decoder PdfRecord
pdfRecordDecoder =
    Decode.map2 PdfRecord
        (field "name" Decode.string)
        (field "path" Decode.string)

getPdf : String -> Cmd Msg
getPdf path =
  Http.post
    { url = "/api/viewer/load"
    , expect  = Http.expectWhatever (RemoteData.fromResult >> Loaded)
    , body = Http.stringBody "application/x-www-form-urlencoded" (urlFormEncodedPdfPath path)
    }

-- Have to remove the ? since it appears twice
urlFormEncodedPdfPath : String -> String
urlFormEncodedPdfPath path = String.dropLeft 1 (UrlBuilder.toQuery [ UrlBuilder.string "pdf" path ])