package ch.wesr.slidebuilder

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteMultipleTools
import ai.koog.agents.core.dsl.extension.nodeLLMCompressHistory
import ai.koog.agents.core.dsl.extension.nodeLLMRequestMultiple
import ai.koog.agents.core.dsl.extension.nodeLLMSendMultipleToolResults
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.core.dsl.extension.onMultipleToolCalls
import ai.koog.agents.core.environment.ReceivedToolResult
import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.tool.AskUser
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.agents.local.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.OllamaModels
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    // default ollama host "http://localhost:11434"
    val executor: PromptExecutor = simpleOllamaAIExecutor()

    // Create tool registry with xxx tools
    val toolRegistry =
        ToolRegistry {
            // Special tool, required with this type of agent.
            tool(AskUser)
            tool(SayToUser)
            tools(CalculatorTools().asTools())
        }

    val strategy =
        strategy("test") {
            val nodeCallLLM by nodeLLMRequestMultiple()
            val nodeExecuteToolMultiple by nodeExecuteMultipleTools(parallelTools = true)
            val nodeSendToolResultMultiple by nodeLLMSendMultipleToolResults()
            val nodeCompressHistory by nodeLLMCompressHistory<List<ReceivedToolResult>>()

            edge(nodeStart forwardTo nodeCallLLM)

            edge(
                (nodeCallLLM forwardTo nodeFinish)
                    transformed { it.first() }
                    onAssistantMessage { true },
            )

            edge(
                (nodeCallLLM forwardTo nodeExecuteToolMultiple)
                    onMultipleToolCalls { true },
            )

            edge(
                (nodeExecuteToolMultiple forwardTo nodeCompressHistory)
                    onCondition { _ -> llm.readSession { prompt.messages.size > 100 } },
            )

            edge(nodeCompressHistory forwardTo nodeSendToolResultMultiple)

            edge(
                (nodeExecuteToolMultiple forwardTo nodeSendToolResultMultiple)
                    onCondition { _ -> llm.readSession { prompt.messages.size <= 100 } },
            )

            edge(
                (nodeSendToolResultMultiple forwardTo nodeExecuteToolMultiple)
                    onMultipleToolCalls { true },
            )

            edge(
                (nodeSendToolResultMultiple forwardTo nodeFinish)
                    transformed { it.first() }
                    onAssistantMessage { true },
            )
        }

    // Create agent config with proper prompt
    val agentConfig =
        AIAgentConfig(
            prompt =
                prompt("test") {
                    system("You are a calculator.")
                },
//            model = OpenAIModels.Chat.GPT4o,
            model = OllamaModels.Meta.LLAMA_3_2,
            maxAgentIterations = 50,
        )

    // Create the runner
    val agent =
        AIAgent(
            promptExecutor = executor,
            strategy = strategy,
            agentConfig = agentConfig,
            toolRegistry = toolRegistry,
        ) {
            handleEvents {
                onToolCall = { tool: Tool<*, *>, toolArgs: Tool.Args ->
                    println("Tool called: tool ${tool.name}, args $toolArgs")
                }

                onAgentRunError = { strategyName: String, throwable: Throwable ->
                    println("An error occurred: ${throwable.message}\n${throwable.stackTraceToString()}")
                }

                onAgentFinished = { strategyName: String, result: String? ->
                    println("Result: $result")
                }
            }
        }

    runBlocking {
        agent.run("(10 + 20) * (5 + 5) / (2 - 11)")
    }
}
