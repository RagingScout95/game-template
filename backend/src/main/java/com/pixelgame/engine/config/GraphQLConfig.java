package com.pixelgame.engine.config;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * GraphQL Configuration
 * Custom ID scalar to handle Long IDs properly (not UUIDs)
 */
@Configuration
public class GraphQLConfig {
    
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(GraphQLScalarType.newScalar()
                        .name("ID")
                        .description("ID scalar that handles Long values")
                        .coercing(new Coercing<Long, String>() {
                            @Override
                            public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
                                if (dataFetcherResult instanceof Long) {
                                    return String.valueOf(dataFetcherResult);
                                }
                                if (dataFetcherResult instanceof Number) {
                                    return String.valueOf(((Number) dataFetcherResult).longValue());
                                }
                                if (dataFetcherResult instanceof String) {
                                    return (String) dataFetcherResult;
                                }
                                throw new CoercingSerializeException("Expected Long or String, got: " + dataFetcherResult.getClass().getName());
                            }

                            @Override
                            public Long parseValue(Object input) throws CoercingParseValueException {
                                if (input instanceof Long) {
                                    return (Long) input;
                                }
                                if (input instanceof Number) {
                                    return ((Number) input).longValue();
                                }
                                if (input instanceof String) {
                                    try {
                                        return Long.parseLong((String) input);
                                    } catch (NumberFormatException e) {
                                        throw new CoercingParseValueException("Cannot parse ID as Long: " + input);
                                    }
                                }
                                throw new CoercingParseValueException("Expected Long or String, got: " + input.getClass().getName());
                            }

                            @Override
                            public Long parseLiteral(Object input) throws CoercingParseLiteralException {
                                if (input instanceof String) {
                                    try {
                                        return Long.parseLong((String) input);
                                    } catch (NumberFormatException e) {
                                        throw new CoercingParseLiteralException("Cannot parse ID as Long: " + input);
                                    }
                                }
                                if (input instanceof Number) {
                                    return ((Number) input).longValue();
                                }
                                throw new CoercingParseLiteralException("Expected String or Number, got: " + input.getClass().getName());
                            }
                        })
                        .build());
    }
}

