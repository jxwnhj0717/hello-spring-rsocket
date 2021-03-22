## 管理执行线程

在某些业务场景，希望为消息绑定处理线程。比如，根据玩家id分配线程，保证单个玩家的消息顺序处理，多个玩家的消息并行处理。为此，需要在消息中传递玩家标识，用于确定线程，并为处理逻辑绑定线程。

### 在消息中传递客户端标识

一个玩家创建一个客户端，服务端接收请求时需要知道消息是谁传过来的。

在消息头中传递客户端标识：

1. 客户端的RSocketClientRequester.route()方法中将客户端标识加入消息头。
2. 服务端的RSocketMessageHandlerConfig.getRSocketStrategies()方法中注册MediaType.APPLICATION_JSON，用于解析对应的消息头。

逻辑处理函数通过方法参数获取客户端标识：

方法一：通过@Header指定参数获取哪一个header，参考：[rsocket-messagemapping](https://docs.spring.io/spring-framework/docs/5.3.5/reference/html/web-reactive.html#rsocket-annot-messagemapping) 。

方法二：定义HandlerMethodArgumentResolver，这样省去在每个处理函数上加@Header注解的麻烦，参考RSocketMessageHandlerCustomizerConfig。



### 为消息处理器指定执行线程

RSocketMessageHandlerConfig.getRSocketMessageHandler()为handleMessage()指定执行线程。

### 为请求响应指定执行线程

使用RSocketClientRequester发送消息，send和retrieve之后的回调会在指定的线程执行。RSocketClientRequester.DefaultRequestSpec指定了回调的执行线程。