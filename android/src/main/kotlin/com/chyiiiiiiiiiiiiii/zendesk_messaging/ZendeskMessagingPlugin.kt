package com.chyiiiiiiiiiiiiii.zendesk_messaging

import ZendeskMessaging
import android.app.Activity
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
/** ZendeskMessagingPlugin */
class ZendeskMessagingPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private val tag = "[ZendeskMessagingPlugin]"
    private lateinit var channel: MethodChannel
    var activity: Activity? = null
    var isInitialized: Boolean = false

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
        val sendData: Any? = call.arguments
        val zendeskMessaging = ZendeskMessaging(this, channel)

        when (call.method) {
            "initialize" -> {
                if (isInitialized) {
                    println("$tag - Messaging is already initialized!")
                    return
                }
                val channelKey = call.argument<String>("channelKey")!!
                zendeskMessaging.initialize(channelKey)
            }
            "show" -> {
                if (!isInitialized) {
                    println("$tag - Messaging needs to be initialized first")
                    return
                }
                zendeskMessaging.show()
            }
            "isInitialized" -> {
                result.success(isInitialized)
            }
            "loginUser" -> {
                if (!isInitialized) {
                    println("$tag - Messaging needs to be initialized first")
                    return
                }

                try {
                    val jwt = call.argument<String>("jwt")
                    if (jwt == null || jwt.isEmpty()) {
                        throw Exception("JWT is empty or null")
                    }
                    zendeskMessaging.loginUser(jwt)
                } catch (err: Throwable) {
                    println("$tag - Messaging::login invalid arguments. {'jwt': '<your_jwt>'} expected !")
                    println(err.message)
                    return
                }
            }
            "logoutUser" -> {
                if (!isInitialized) {
                    println("$tag - Messaging needs to be initialized first")
                    return
                }
                zendeskMessaging.logoutUser()
            }
            "getUnreadMessageCount" -> {
                if (!isInitialized) {
                    println("$tag - Messaging needs to be initialized first")
                    return
                }
                result.success(zendeskMessaging.getUnreadMessageCount())
            }
            "setConversationTags" -> {
                if (!isInitialized) {
                    println("$tag - Messaging needs to be initialized first")
                    return
                }

                try {
                    val tags = call.argument<List<String>>("tags")
                    if (tags == null) {
                        throw Exception("tags is empty or null")
                    }

                    zendeskMessaging.setConversationTags(tags)
                } catch (err: Throwable) {
                    println("$tag - Messaging::setConversationTags invalid arguments. {'tags': '<your_tags>'} expected !")
                    println(err.message)
                    return
                }
            }
            "clearConversationTags" -> {
                if (!isInitialized) {
                    println("$tag - Messaging needs to be initialized first")
                    return
                }
                zendeskMessaging.clearConversationTags()
            }
            "invalidate" -> {
                if (!isInitialized) {
                    println("$tag - Messaging is already on an invalid state")
                    return
                }
                zendeskMessaging.invalidate()
            }
            else -> {
                result.notImplemented()
            }
        }

        if (sendData != null) {
            result.success(sendData)
        } else {
            result.success(0)
        }
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "zendesk_messaging")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

}