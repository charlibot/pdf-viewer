module Main exposing (..)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Json.Decode as Decode exposing (Decoder, field, string)
import RemoteData exposing (..)
import Url.Builder as UrlBuilder
import Table exposing (defaultCustomizations)


-- MAIN
main =
  Browser.element
    { init = init
    , update = update
    , subscriptions = subscriptions
    , view = view
    }

-- MODEL
type alias Model =
    { pdfs : WebData Pdfs
    , tableState : Table.State
    , query : String
    }

type alias Pdfs = List PdfRecord
type alias PdfRecord =
    {  name : String
    ,  path : String
    }

init : () -> (Model, Cmd Msg)
init _ =
  ({ pdfs = Loading, tableState = Table.initialSort "Name", query = "" }, getPdfs)

-- UPDATE
type Msg
  = GotPdfs (WebData Pdfs)
  | LoadPdf String
  | Loaded (WebData ())
  | SetQuery String
  | SetTableState Table.State

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    GotPdfs response ->
      ({model | pdfs = response}, Cmd.none)

    LoadPdf pdfPath ->
      (model, getPdf pdfPath)

    Loaded _ ->
        (model, Cmd.none)

    SetQuery newQuery ->
      ( { model | query = newQuery }
      , Cmd.none
      )

    SetTableState newState ->
      ( { model | tableState = newState }
      , Cmd.none
      )



-- SUBSCRIPTIONS
subscriptions : Model -> Sub Msg
subscriptions _ =
  Sub.none

-- VIEW
view : Model -> Html Msg
view model =
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
        [ Table.stringColumn "Name" .name]
    , customizations =
        { defaultCustomizations | tableAttrs = tableAttrs, rowAttrs = loadPdf }
    }

loadPdf : PdfRecord -> List (Attribute Msg)
loadPdf pdf = [ onClick (LoadPdf pdf.path) ]

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