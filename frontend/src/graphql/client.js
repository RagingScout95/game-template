import { GraphQLClient } from 'graphql-request';

const endpoint = 'http://localhost:8080/graphql';

export const createGraphQLClient = (token = null) => {
  const headers = {};
  
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  
  return new GraphQLClient(endpoint, { headers });
};

export const graphqlClient = createGraphQLClient();

