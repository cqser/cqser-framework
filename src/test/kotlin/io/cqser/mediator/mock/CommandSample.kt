package io.cqser.mediator.mock

import io.cqser.mediator.core.Command
import io.cqser.mediator.core.CommandHandler

class SayHelloCommand(val message: String): Command

class SayHelloCommandHandler: CommandHandler<SayHelloCommand> {

    override fun handle(command: SayHelloCommand) {
        println(command.message)
    }
}