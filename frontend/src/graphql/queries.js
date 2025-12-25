import { gql } from 'graphql-request';

export const LOGIN_MUTATION = gql`
  mutation Login($input: LoginInput!) {
    login(input: $input) {
      token
      user {
        id
        username
        name
        role
      }
    }
  }
`;

export const REGISTER_PLAYER_MUTATION = gql`
  mutation RegisterPlayer($input: RegisterPlayerInput!) {
    registerPlayer(input: $input) {
      token
      user {
        id
        username
        name
        empId
        role
      }
    }
  }
`;

export const GET_ALL_GAMES_QUERY = gql`
  query GetAllGames {
    getAllGames {
      id
      name
      description
      active
    }
  }
`;

export const START_GAME_MUTATION = gql`
  mutation StartGame($gameId: ID!) {
    startGame(gameId: $gameId) {
      progress {
        id
        playerPosition
        completed
      }
      currentLevel {
        id
        name
        mapData
      }
      currentScenario {
        id
        name
        description
      }
      currentStep {
        id
        name
        type
        movementMode
        dialog {
          id
          speakerName
          text
        }
        audio {
          id
          url
          loop
        }
      }
      availableNPCs {
        id
        name
        spriteSheet
        movementType
        initialPosition
      }
    }
  }
`;

export const GET_GAME_STATE_QUERY = gql`
  query GetGameState($gameId: ID!) {
    getGameState(gameId: $gameId) {
      progress {
        id
        playerPosition
        completed
      }
      currentLevel {
        id
        name
        mapData
      }
      currentScenario {
        id
        name
        description
        mcq {
          id
          question
          options
        }
      }
      currentStep {
        id
        name
        type
        movementMode
        dialog {
          id
          speakerName
          text
        }
      }
      availableNPCs {
        id
        name
        spriteSheet
        movementType
        initialPosition
      }
    }
  }
`;

export const SUBMIT_MCQ_ANSWER_MUTATION = gql`
  mutation SubmitMCQAnswer($input: SubmitMCQAnswerInput!) {
    submitMCQAnswer(input: $input) {
      id
      selectedOptionIndex
      isCorrect
    }
  }
`;

export const UPDATE_PLAYER_POSITION_MUTATION = gql`
  mutation UpdatePlayerPosition($input: UpdatePlayerPositionInput!) {
    updatePlayerPosition(input: $input) {
      id
      playerPosition
    }
  }
`;

export const TRIGGER_EVENT_MUTATION = gql`
  mutation TriggerEvent($input: TriggerEventInput!) {
    triggerEvent(input: $input) {
      progress {
        id
        playerPosition
      }
      currentStep {
        id
        name
        type
        movementMode
        dialog {
          speakerName
          text
        }
      }
    }
  }
`;

