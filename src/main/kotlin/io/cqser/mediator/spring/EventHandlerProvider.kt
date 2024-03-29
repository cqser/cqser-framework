/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cqser.mediator.spring

import io.cqser.mediator.core.EventHandler
import org.springframework.context.ApplicationContext
import kotlin.reflect.KClass

/**
 * A wrapper around an EventHandler
 *
 * @author Joseph Kratz
 * @since 1.0
 * @property applicationContext ApplicationContext from Spring used to retrieve beans
 * @property type Tyoe of EventHandler
 */
internal class EventHandlerProvider<T>(
    private val applicationContext: ApplicationContext,
    private val type: KClass<T>
) where T: EventHandler<*> {

    fun get(): T {
        return applicationContext.getBean(type.java)
    }
}