module Main exposing (..)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Json.Decode as Decode exposing (Decoder, field, string)

-- MAIN
main =
  Browser.element
    { init = init
    , update = update
    , subscriptions = subscriptions
    , view = view
    }

-- MODEL
type Model = Failure | Loading | Success Pdfs

type alias Pdfs = List PdfRecord
type alias PdfRecord =
    {  name : String
    }

init : () -> (Model, Cmd Msg)
init _ =
  (Loading, getPdfs)

-- UPDATE
type Msg
  = GotPdfs (Result Http.Error Pdfs)
  | ViewPdf String

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    GotPdfs result ->
      case result of
        Ok pdfs ->
          (Success pdfs, Cmd.none)

        Err _ ->
          (Failure, Cmd.none)

    ViewPdf pdfId ->
      (model, Cmd.none)



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
        , viewPdfs model
        ]
    ]

viewPdfs : Model -> Html Msg
viewPdfs model =
  case model of
    Failure ->
      text "Failed to get the pdfs."

    Loading ->
      text "Loading pdfs..."

    Success pdfs ->
      table [ class "table is-striped is-fullwidth is-hoverable" ] (viewHeaders :: List.map viewPdf pdfs)

viewHeaders : Html Msg
viewHeaders = tr [] [ th [] [ text "Name" ] ]

viewPdf : PdfRecord -> Html Msg
viewPdf pdf =
    tr [ onClick (ViewPdf pdf.name) ] [ td [] [ text pdf.name ]
          ]

-- HTTP
getPdfs : Cmd Msg
getPdfs =
  Http.get
    { url = "/api/pdfs"
    , expect  = Http.expectJson GotPdfs recordsDecoder
    }

recordsDecoder : Decoder Pdfs
recordsDecoder = Decode.list pdfRecordDecoder

pdfRecordDecoder : Decoder PdfRecord
pdfRecordDecoder =
    Decode.map PdfRecord
        (field "name" Decode.string)
