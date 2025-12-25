package com.pixelgame.engine.config;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {
    
    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        log.error("GraphQL Error in {}: {}", env.getExecutionStepInfo().getPath(), ex.getMessage(), ex);
        
        // Return a more detailed error message
        return GraphqlErrorBuilder.newError()
                .message(ex.getMessage() != null ? ex.getMessage() : "Internal server error")
                .path(env.getExecutionStepInfo().getPath())
                .build();
    }
}

