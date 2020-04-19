module PdfView exposing (..)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Json.Decode as Decode exposing (Decoder, field, string)
import RemoteData exposing (RemoteData(..), WebData)

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
    { pdf : WebData PdfModel
    }

type alias PdfModel =
    { name : String
    , pageNumber : Int -- Should this be in the model?
    , numPages : Int
    }


init : () -> (Model, Cmd Msg)
init _ =
  (Model NotAsked, Cmd.none)

-- UPDATE
type Msg
  = GotPdf (WebData PdfModel)
  | NextPage


update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    GotPdf response ->
        ( { model | pdf = response }, Cmd.none )

    NextPage ->
        let (pdf, command) = RemoteData.update (\t -> (nextPage t, Cmd.none)) model.pdf
        in
        ( { model | pdf = pdf}, command)

nextPage : PdfModel -> PdfModel
nextPage pdf = if pdf.pageNumber + 1 <= pdf.numPages then { pdf | pageNumber = pdf.pageNumber + 1 } else pdf

-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.none

-- VIEW


view : Model -> Html Msg
view model =
    section [ class "section" ]
        [ div [ class "container" ]
            [ viewModel model ]
        ]

viewModel : Model -> Html Msg
viewModel model =
    case model.pdf 