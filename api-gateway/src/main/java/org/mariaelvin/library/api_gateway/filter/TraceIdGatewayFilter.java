package org.mariaelvin.library.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class TraceIdGatewayFilter implements GlobalFilter, Ordered {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String existingTraceId = exchange.getRequest()
                .getHeaders()
                .getFirst(TRACE_ID_HEADER);

        String traceId = existingTraceId != null && !existingTraceId.isBlank()
                ? existingTraceId
                : UUID.randomUUID().toString();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(request -> request.header(TRACE_ID_HEADER, traceId))
                .build();

        mutatedExchange.getResponse()
                .getHeaders()
                .add(TRACE_ID_HEADER, traceId);

        log.info(
                "Gateway request started. traceId={}, method={}, path={}",
                traceId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath()
        );

        return chain.filter(mutatedExchange)
                .doFinally(signalType ->
                        log.info(
                                "Gateway request completed. traceId={}, method={}, path={}, signal={}",
                                traceId,
                                exchange.getRequest().getMethod(),
                                exchange.getRequest().getURI().getPath(),
                                signalType
                        )
                );
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
