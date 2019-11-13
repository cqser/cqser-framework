package io.cqser.mediator.mock

import io.cqser.mediator.core.Request
import io.cqser.mediator.core.RequestHandler
import java.math.BigDecimal

data class CalculateOrderTotalRequest(val price: BigDecimal,
                                 val qty: BigDecimal): Request<BigDecimal>

class CalculateOrderTotalRequestHandler: RequestHandler<CalculateOrderTotalRequest, BigDecimal> {

    override fun handle(request: CalculateOrderTotalRequest): BigDecimal {
        return request.price.multiply(request.qty)
    }
}

