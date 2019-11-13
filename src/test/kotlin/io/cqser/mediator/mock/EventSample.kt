package io.cqser.mediator.mock

import io.cqser.mediator.core.Event
import io.cqser.mediator.core.EventHandler

data class OrderCreatedEvent(val orderId: Int): Event

class OrderCreationListener: EventHandler<OrderCreatedEvent> {

    override fun handle(event: OrderCreatedEvent) {
        println("Order created with ID: ${event.orderId}")
    }
}