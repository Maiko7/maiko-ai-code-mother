# 思路

1. 创建新项目的时候，你要做的是**先选git**，这就是大厂标配，鱼皮教给你的，再也不要用你的那个什么复制文件夹的操作了。

2. 你在引入一个依赖比如Knife4j的时候，你怎么去开始呢？就是去官网文档看，它里面会有就是一个Start。或者现在的新思路你不用去官网了，你扔给**大模型**（通义千问、GPT）你让它**去官网读**，然后帮你总结，帮你写yaml、写配置类。

3. 解决Git上传不了Github的问题

   ### **使用 Gitee 的“双向同步”功能（最推荐，最稳）**

   既然 Gitee 你能连上，GitHub 你连不上，那就让 Gitee 帮你把代码“顺手”推过去。这不需要你电脑联网 GitHub。

   **操作步骤：**

   1. **去 Gitee 网页后台**
      打开你的 Gitee 仓库页面 -> 点击 **“管理”**。

   2. **找到“镜像仓库”**
      在左侧菜单栏找 **“仓库设置”** -> **“镜像仓库”** (或者叫“外部仓库”、“同步”之类的)。

   3. **填写 GitHub 地址**

      - **远程仓库地址**：填 `https://github.com/Maiko7/maiko-ai-code-mother.git`

      - **用户名**：填你的 GitHub 用户名 (`Maiko7`)

      - 密码/Token

        ：这里不能填密码了，需要填 GitHub 的 Token。

        - *如果你不知道怎么生成 Token，可以先去 GitHub 看一下，或者我下一条消息教你。*

   4. **点击“开始同步”**
      Gitee 会自动把代码拉过去，推送到你的 GitHub 上。

4. 你写项目的时候，你上来**不要一股脑的直接开始初始化项目**。你要先想好，诶我先**需求分析**，这个项目比如用户模块：那用户模块包含什么，比如用户注册、登录、获取当前用户等等，你先分析一下这个模块的的需求分析；然后你再**方案设计**，比如说用户登录那你肯定要设计数据库表吧？用户的登录流程图这些。

5. 你还要注意一个点就是最近AI编程助手的那些，你想啊，如果你开发一个比如这个AI零代码应用生成平台。那你直接把整个的需求扔给他吗？这么大的项目，那你肯定也是**一个项目一个项目的开发**啊，比如说先开发用户模块，再开发AI模块，再开发平台。然后每次一个git，这样不会出错，而且AI编程也能更好的理解和完成，而不是一股脑的去生成整个项目。

6. ```java
   INDEX idx_userName (userName)
   为什么鱼皮给userName加了索引呢？你不要觉得 就是这个用户表拿过来我就直接复制就是，你要想为什么。它是需求分析得出来的，比如根据用户名称搜索用户，那搜索用户比较常见的就是根据名称搜索，这就是为什么它加了这个名称的索引。所以给什么加索引，不是说你加就加，你是要经过需求分析的。
      
   ```

7. Service层能调Mapper就调Mapper。User user = this.mapper.selectOneByQuery(queryWrapper);

8. 不是说什么都抽取成常量放到常量接口里面去，而是你看它会不会多次重复使用，不是说搞个数字你就要抽取。要合理，不要为了抽而抽。

9. > git流程，就是你创新文件一样的，现在用git。
   >
   > master 永远稳定 → 新功能开分支 → 分支里开发 → 完成合并回 master → 删分支 → 打 TAG 标记版本。
   >
   > ## ✔ 开发新功能前要不要打 TAG？
   >
   > **不用！**
   >
   > TAG 是**功能完成后**打的，不是开发前。
   >
   > ## ✔ 我不确定能不能完成怎么办？
   >
   > **就在功能分支里写，写不出来直接删分支，master 毫发无损！**
   >
   > ## ✔ 打 TAG2 代表新版本吗？
   >
   > 对！
   >
   > - v1.0.0 = 最初版
   > - v1.0.1 = 加了登录
   > - v1.0.2 = 加了微信登录
   > - v1.1.0 = 大版本更新

10. 其实最重要的不是写代码，而是你写代码之前的思路，步骤，流程。这些是最重要的，这些都搞定了再去写代码就会很快，而且现在还有AI编程助手。

11. **注意写Prompt的时候，你可以让AI帮你生成**，然后给它参考的比如https://help.aliyun.com/zh/model-studio/prompt-engineering-guide。

12. ```yml
    你一定要学会鱼皮这个上传github的时候，你记得把你的花钱的比如说调用deepseek的API放到application-local.yml配置文件下然后通过application.yml去。
    记得在.gitignore中，防止它被提交到网上了。不然大家一起刷你的流量花你的钱！！
    profiles:
      active: local
      
     ### CUSTOM ###
    application-local.yml
    ```

13. 门面模式是什么呢？就这里面而言，这个门面的作用是统一负责代码生成和保存功能。你想你要让AI生成代码是不是你先调用AI大模型，先得到字符串或者结构化输出的对象，然后我再调用文件的保存器保存对象。那你觉得你作为一个调用方，我需要关注这些吗？我肯定不需要啊，我只调用这个门面，你直接给我返回好生成的文件，我根本不关心你怎么调用AI大模型，你怎么解析字符串的，这就是门面模式的作用。

14. ```java
    你这个AiCodeGeneratorFacade里面是有generateAndSaveMultiFileCodeStream和generateAndSaveHtmlCodeStream的，那如果就是这个try catch是我加的，我一动我两个地方都要改，那下次我要加个别的是不是也是一样的，一动都要动。这个时候你就应该发现问题了，你就要想办法把它抽出来了
    
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        StringBuilder codeBuilder = new StringBuilder();
        return result.doOnNext(chunk -> {
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            try {
                String completeMultiFileCode = codeBuilder.toString();
                MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeMultiFileCode);
                File saveDir = CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
                log.info("保存成功，目录为：{}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败：{}", e.getMessage());
            }
        });
    }
    ```

15. 策略模式：想要去实现不同类型生成的时候，我们把每种生成的解析方式定义成一个算法，对于不同的代码类型，我们可以直接切换不同的解析器一行代码即可切换，后续增加也不影响。<img src="C:\Users\73450\AppData\Roaming\Typora\typora-user-images\image-20260411212754431.png" alt="image-20260411212754431" style="zoom:50%;" />

16. 为什么你解析器的那里你用的是策略模式而你文件保存部分你要用模板方法模式呢？因为文件保存器它是有流程的，而解析器就是创建对象，提取代码，封装就好了。你比如说文件保存器第1步构造独立的目录，第2步写入文件，第3步返回文件对象，所以你会发现文件保存器它是**有流程的**。当有流程的时候你就要考虑到能不能去复用这些流程，你HTML的保存还是多文件的保存，你都要生成唯一目录吧？你都要保存吧？所以对于这种情况可以考虑使用模板方法模式。什么是模板方法模式呢？它的作用就是我先定义一套模板，我在抽象父类中定义你们所有的保存器都必须按照这个流程来进行操作，父类定义流程。至于你子类具体怎么实现，比如怎么保存代码怎么定义目录，我不关心，你可以在子类自己去实现，但你必须遵循我父类的流程。必须要构造唯一的目录然后再去执行保存然后再去执行一些校验操作等等。
17. 

# 编码

1. ```java
           // 方式一：格式化输出 dataSourceConfig
           System.out.println("dataSourceConfig 内容：");
           for(Map.Entry<String, Object> entry : dataSourceConfig.entrySet()) {
               System.out.println(entry.getKey() + " = " + entry.getValue());
           }
   //        方式二：参数 + 箭头 + 方法体
   //        dataSourceConfig.forEach((key, value) -> System.out.println(key + " = " + value));
   ```

2. ```
   Dict dict = YamlUtil.loadByPath("application.yml");
   Map<String, Object> dataSourceConfig = dict.getByPath("spring.datasource");
   
   // 直接读取单个值，自动转 String
   String url = dict.getStr("spring.datasource.url");
   
   // 直接读取单个值，自动转 int
   int port = dict.getInt("server.port");
   
   问getByPath和getInt区别是什么？
       就是一个取的是对象，整个spring.datasource下面的配置，而你这个String url = dict.getStr("spring.datasource.url");取的就只是url这一个。
   ```

| 方法                | 作用                          | 返回类型     | 适用场景                          |
| ------------------- | ----------------------------- | ------------ | --------------------------------- |
| **getByPath**       | 读取路径对应的值              | **Object**   | 读取**对象、Map、List、整块配置** |
| **getStr / getInt** | 读取路径对应的值 + 自动转类型 | String / int | 读取**单个配置项**                |

3. 它这里为什么通用的有了BusinessException还要ThrowUtils呢？因为如果你只是BusinessException那你就要每次都是new BusinessException(ErrorCode.NOT_FOUND_ERROR)这样，你要每次new，不优雅。BaseResponse和ResultUtils也是一样，就是说每次都是new BaseResponse那不如直接封装ResultUtils。

4. GLobalExceptionHandLer就是保证你后端项目的健壮性，不能说一个用户出错了导致后端全错了，这肯定不行的，所以为了防止项目中任何的异常导致后端项目退出，就可以新建一个这个类。

5. GLobalExceptionHandLer中**🧐 为什么要加** `@Hidden`**？**
   这种情况是因为 **Spring Boot 3.4+** 和 **SpringDoc (OpenAPI 3)** 在扫描 Bean 时的机制发生了变化，导致 `@RestControllerAdvice` 被错误地当成了一个普通的 API 接口显示在文档里。

#### **现象**

在不加 `@Hidden` 时，Knife4j/SpringDoc 会尝试去解析 `GlobalExceptionHandler` 这个类。因为它上面有 `@RestController` 的特性，文档工具会误以为它也是一个 Controller，试图把它显示在接口列表中。

#### **后果**

- **文档报错**：因为它没有标准的 `@RequestMapping` 路径，或者方法签名不符合标准 Controller 的规范，生成文档时可能会报错。
- **界面混乱**：它可能会作为一个奇怪的接口出现在你的文档列表里，用户点进去也调不通。

#### **解决**

加上 `@Hidden`（来自 `io.swagger.v3.oas.annotations.media.Schema` 包），就是告诉 SpringDoc：**“别扫描我，我只是个幕后处理异常的，我不配出现在文档页面上。”**

6. PageRequest就是你之后需要分页请求的你直接继承它即可，就具备了这些参数。

7. DeleteRequest就是删除包装类，因为都是根据id删除的嘛，所以可以直接搞一个common的。

8. ```java
   /**
        * id
        * 这种默认自增的id生成策略有个问题，很容易被人爬虫
        */
   //    @Id(keyType = KeyType.Auto)
   这种默认自增的id生成策略有个问题，很容易被人爬虫这句话什么意思举个例子？
      所谓的“被人爬虫”，在这里并不是指别人写个脚本自动下载你的网页，而是指竞争对手或恶意用户可以通过 ID 的规律，轻易推算出你的业务数据量，甚至窃取你的所有数据。为了防止数据被遍历和隐藏业务增长数据，现在的互联网项目（尤其是对外暴露 API 的项目）通常都推荐使用雪花算法而不是数据库自增。
       🚨 场景举例：外卖店的秘密
   假设你开了一家非常火爆的“马iko炸鸡店”，你的订单表 order 使用了 KeyType.Auto（数据库自增 ID）。
   自增 ID 的规律
   第 1 个订单的 ID 是 1
   第 2 个订单的 ID 是 2
   ...
   今天的最后一个订单 ID 是 100
   竞争对手的“爬虫”攻击
   你的竞争对手“小胡汉堡”发现你的订单 ID 是显示在 URL 上的，比如：https://maiko.com/order/100。
   他只需要做一个极其简单的操作：
   早上 9 点，他访问 .../order/1（或者随便猜一个今天的起始号）。
   晚上 9 点，他试着访问 .../order/101，发现页面不存在（404）。
   结论：他立刻知道你今天一共卖了 100 单。
   这就叫“被人爬虫”了核心商业机密。 他不需要破解你的数据库，只需要看 ID 的变化幅度，就能算出你的日营业额、月营业额，甚至根据 ID 的增量知道你现在生意是不是变差了。
   ```

   

9. Java 的多态性规定：**实现类的方法访问权限，不能低于接口（或父类）中定义的权限。**

   ```java
   private String getEncryptPassword(String userPassword) {
       String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));
       return encryptPassword;
   }
   所以你UserServiceImpl里面不能用private，因为UserService接口肯定是public的方法，那你实现类不能低于父类定义的权限。
   ```

10. 

# 问题

>  检查 Project SDK：File -> Project Structure -> Project，确保 SDK 和 Language level 都选了 21。
>  检查 Maven Runner：File -> Settings -> Build, Execution, Deployment -> Build Tools -> Maven -> Runner。
>  在 JRE 选项里，确保选中的是 21，而不是默认的旧版本。
>
> 这两个是什么？

这两个设置分别对应**代码编译**和**程序运行**时的 Java 环境，也就是“写代码用的 JDK”和“跑代码用的 JRE”。

具体解释如下：

### **Project SDK（项目 SDK）**

- **这是什么？**
  这是告诉 IntelliJ IDEA：“我这个项目是用哪个版本的 JDK 来写的”。它决定了你在写代码时能用哪些新语法（比如 Java 21 的虚拟线程），以及代码最终会被编译成哪个版本的字节码。
- **为什么要改？**
  如果你的代码里用了 Java 21 的语法（比如 `var` 关键字的新用法，或者 Record 类），但这里选的是 Java 8，IDEA 就会报红，提示语法错误。
- 怎么检查？
  - 按 `Ctrl + Alt + Shift + S` (Windows) 或 `Cmd + ;` (Mac) 打开 Project Structure。
  - 左边选 **Project**。
  - 右边的 **SDK** 必须选 `1.8` / `11` / `17` / `21` 中的一个（对应你想用的版本）。
  - 右边的 **Language level** 也要选对应的版本。

### **Maven Runner JRE（Maven 运行环境）**

- **这是什么？**
  这是告诉 Maven：“当你运行 `mvn compile` 或 `mvn spring-boot:run` 这些命令时，请用哪个版本的 Java 来执行”。
  **注意**：这个设置**独立于** Project SDK。即使你 Project SDK 选了 21，如果这里选的是 Java 8，Maven 还是会用 Java 8 去跑编译任务，从而导致你遇到的那个 `不支持发行版本 21` 的错误。
  
- **为什么要改？**
  这就是你报错的直接原因。Maven 插件需要运行在一个 Java 环境中，如果这个环境太老，它就看不懂 `pom.xml` 里要求的 `release 21`。
  
- 怎么检查？
  - 打开 Settings (`Ctrl + Alt + S` 或 `Cmd + ,`)。
  
  - 找到 **Build, Execution, Deployment** -> **Build Tools** -> **Maven** -> **Runner**。
  
  - 在右边的 **JRE** 下拉框中，确保选中的是你的 **JDK 21**（或者你项目对应的版本），而不是默认的 `1.8` 或其他旧版本。
  
    

--------



> 问题1：// 4. 接口不需要权限，直接放行        if (StrUtil.isBlank(mustRole)) {            return joinPoint.proceed();        } 你这个是什么情况才会出现？
>
> 答案：:当你只写 @AuthCheck，不写 mustRole 时，就会走这里！
>
> 问题2：不需要权限直接放行，那你还多此一举干嘛？
>
> 你这句话**问到点子上了，非常犀利**！
>
> 我直接跟你说**最真实、最本质的原因**，不绕弯子：
>
> # 核心一句话
>
> **`@AuthCheck` 不写 mustRole，不是 “不需要权限”，而是 “不需要【角色权限】，但必须【已登录】”！**
>
> ------
>
> # 你现在这段代码真正的逻辑是：
>
> ```java
> if (StrUtil.isBlank(mustRole)) {
>     return joinPoint.proceed();
> }
> ```
>
> **不是：任何人都能访问而是：只要登录了，就能访问**
>
> 因为在这行**前面**，你已经写了：
>
> ```java
> User loginUser = userService.getLoginUser(request);
> ```
>
> 👉 **`getLoginUser` 内部本身就会校验是否登录**
>
> 没登录会直接抛：`NOT_LOGIN` 异常
>
> ------
>
> # 所以真实流程是这样：
>
> ## ① `@AuthCheck`
>
> - 必须登录
> - 不校验是 admin 还是 user
> - 登录就能进
>
> ## ② `@AuthCheck(mustRole = "admin")`
>
> - 必须登录
> - 还必须是管理员
> - 否则拒绝
>
> ## ③ 不加注解
>
> - 不需要登录
> - 游客都能访问
>
> ------
>
> # 那你这句：
>
> ```java
> if (StrUtil.isBlank(mustRole)) {
>     return joinPoint.proceed();
> }
> ```
>
> **作用就是：“只要求登录，不要求角色” 的场景**
>
> ------
>
> # 如果你真的想 “完全不需要权限、游客也能访问”
>
> 那应该：
>
> **干脆不加 `@AuthCheck` 注解**
>
> 而不是加了注解又设为空。



----



> 什么是运行异常，什么是收件异常？
>
> # 1. 运行时异常（RuntimeException）
>
> **不用你抓，代码也能编译通过，出问题才炸。**
>
> - 空指针 `NullPointerException`
> - 除零异常 `ArithmeticException`
> - 数组越界 `IndexOutOfBoundsException`
> - 你自己的 `BusinessException`
>
> 特点：
>
> - **不要求必须 try/catch**
> - 不要求必须 `throws`
> - 出问题时会**往上抛**
> - 能被你的 **GlobalExceptionHandler 自动捕获**
>
> ------
>
> # 2. 受检异常（Checked Exception）
>
> **编译器强制你处理，不抓就报错，不让你运行。**
>
> 比如：
>
> - `WxErrorException`（微信 SDK 的异常）
> - `IOException`（文件读写）
> - `SQLException`（数据库）
> - `ParseException`（日期解析）
>
> 特点：
>
> - **必须 try/catch 或者 throws**
> - 否则代码直接爆红，编译不通过
> - **不会被 RuntimeException 全局捕获**
> - 必须你手动 catch 处理
>
> **运行时异常：代码炸了才报错，不用提前抓**
>
> **受检异常：编译器逼你抓，不抓不让跑**



----



> 为什么我写程序调用api 要花钱？但是我在这里问你我不需要花钱？ 效果不是一样的吗 不都是你生成吗？
>
> ### **🏢 场景不同：展示 vs. 生产**
>
> #### **你在这里（网页/App）问我**
>
> - **目的**：这是阿里的**“大卖场”**。我作为一个强大的模型，被放在这里是为了**展示能力**。
> - **商业模式**：阿里的目的是让你觉得我好用，从而吸引你成为阿里云的客户，或者通过网页端的广告、会员订阅（比如某些高级功能）来赚钱。
> - **成本承担**：这部分算力成本被算作了**“市场营销费用”**或**“研发测试成本”**。简单来说，阿里在“请客”，为了让你以后买单。
>
> #### **你写程序调用 API**
>
> - **目的**：这是**“工业生产”**。你把我的能力接入到你的软件、网站或公司系统里，用来服务你的用户，或者帮你自动写代码赚钱。
> - **商业模式**：你是在利用我的能力**创造价值**（比如你开发了一个APP卖给客户）。既然你赚了钱，或者省了人工成本，那么你就需要为使用的算力付费。
> - **成本承担**：每一次 API 调用，都需要真实的显卡（GPU）在数据中心高速运转，这消耗的是真金白银的电费和硬件损耗。这种大规模的商用消耗，不可能由阿里一直免费买单。



----



> 问题1：如果我UserServiceImpl不加@Service但是我下面的则会报错。
> @Resource
> private RedisTemplate<String, Object> redisTemplate;
>
> 因为如果你不在 `UserServiceImpl` 类上加 `@Service`（或者 `@Component`），Spring 容器根本**不知道这个类的存在**，也就无法管理它。
>
> **@Resource 失效**
> 虽然你在 `UserServiceImpl` 里写了 `@Resource private RedisTemplate...`，但因为 `UserServiceImpl` 自己都不在 Spring 的管理之下，Spring 也就不会帮你执行注入操作。那个 `redisTemplate` 变量将会是 `null`，一用就报空指针异常（NullPointerException）。
>
> 问题2：就是 比如我UserServiceImpl我加了Service然后就是告诉别人，你可以通过@Resource来用我了，但是我点进去看RedisTemplate 它并没有加什么的@service 或者@component啊
>
> 简单来说：
>
> - **你的 `UserServiceImpl`**：是你自己写的代码，Spring 不知道它的存在，所以你要贴 `@Service` 告诉 Spring：“请把它抓进容器里”。
> - **`RedisTemplate`**：是 Spring 官方（或者第三方库）写好的，它**早就被放进容器里了**，所以它身上不需要贴 `@Service`。
>
> 这里有两个核心原因，我为你详细拆解一下：
>
> ### **1. 它是“自动配置”进来的（不用贴标签）**
>
> `RedisTemplate` 是 Spring Boot 官方提供的工具类。当你引入了 `spring-boot-starter-data-redis` 依赖后，Spring Boot 启动时会自动运行一个配置类（叫 `RedisAutoConfiguration`）。
>
> 在这个配置类里，有一段代码大概是这样的（伪代码）：
>
> ```java
> @Configuration // 这是一个配置类
> public class RedisAutoConfiguration {
> 
>     @Bean // 重点看这里！
>     public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
>         // 手动创建一个 RedisTemplate 对象并扔进容器
>         RedisTemplate<String, Object> template = new RedisTemplate<>();
>         template.setConnectionFactory(factory);
>         return template;
>     }
> }
> ```
>
> **关键点：**
>
> - 你的类是用 **`@Component` / `@Service`** 标记的（自动扫描）。
> - 第三方的类（如 `RedisTemplate`）是用 **`@Bean`** 标记在配置类的方法上的（手动注册）。
>
> **结果是一样的**：它们最后都在 Spring 的容器（池子）里。所以当你 `@Resource` 时，Spring 能在池子里找到它。
>
> ### **2. 源码你也改不了啊**
>
> 试想一下，`RedisTemplate` 是 Spring 框架源码里的类。
> 如果它必须加 `@Service` 才能被注入，那岂不是意味着你要去修改 Spring 的源码，给它加上注解，然后重新编译？那太疯狂了！
>
> 为了解决这个问题，Spring 提供了 **`@Bean`** 这种机制，专门用来把那些**“我们无法修改源码的第三方类”**（比如 JDK 自带的 `DataSource`，或者 Redis 的 `RedisTemplate`）注册到容器中。
>
>
> 总结：为什么我们的UserServiceImpl或者Controller加了@Service或者@Controller，其实就是让spring发现我们，然后让其他的地方调用我们这个的时候直接@Resource就能调用了。为什么RedisTemplate没加注解呢？因为系统帮它们加了。





> ```java
> 问题：为什么我只是这样写了一下，它就知道我调用的是DeepSeek？我根本没写Config的配置类啊，没告诉Spring我配置了Deepseek啊。
> @Resource
> private ChatModel chatModel;
> 答案：因为你写了yml配置
> langchain4j:
> open-ai:
>  chat-model:
>    base-url: https://api.deepseek.com
>    api-key: sk-89c2e96b2c4349d1a63639b9ad30151a
>    model-name: deepseek-chat
> 你引入的 langchain4j-spring-boot-starter 这个依赖包里，有一个特殊的类（通常叫 LangChain4jAutoConfiguration）。
> 这个类上有一个注解 @Configuration，它里面包含一段逻辑：
> 逻辑：检查 Spring 容器里有没有 ChatModel。
> 动作：如果没有，就去读取你刚才写的 application.yml 里的配置（base-url, api-key 等）。
> 结果：用这些配置参数，自动 new 出一个 OpenAiChatModel 对象，并把它注册到 Spring 容器里。
> 🧩 为什么能这么“智能”？
> 这基于 Spring Boot 的约定优于配置（Convention over Configuration）原则。
> 约定的 Key：langchain4j.open-ai.chat-model。
> LangChain4j 的开发者和 Spring Boot 的开发者约定好了，只要你在配置文件里写了这个 Key，就代表你想让我帮你创建一个 ChatModel。
> Starter 机制：你引入的依赖通常是 xxx-spring-boot-starter。这种 Starter 包的核心功能就是：“只要你引入我，我就能通过配置自动帮你把 Bean 注册好”。
> 
> 总结：这就是约定大于配置的最好的体现！！！ 就是你只写了yml 然后你就@Resource private ChatModel chatModel;这样一下就出来了deepseek的模型。
> 
> 示例二：还有一个就是我们写
> public interface AiCodeGeneratorService {
> 
>     @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
>     HtmlCodeResult generateHtmlCode(String userMessage);
> }
> 然后测试类这样写。
> void generateHtmlCode() {
>         HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做个不会写代码的Maiko博客，不超过20行");
>         Assertions.assertNotNull(result);
> }
> 你看啊，它直接调用的接口，然后接口也没写实现的东西，就是一个HtmlCodeResult generateHtmlCode(String userMessage);什么代码也没写，它怎么搞到的结果？按道理它是接口它没实现类啊，它怎么可能有东西返回。这就是约定大于配置！
> AiServices.create(...) 这行代码，在运行时帮你“凭空捏造”了一个实现了该接口的类。而AiServices.create(...) 哪里来的呢？LangChain4j 早就内置了自动装配（Auto-configuration）功能。只要你引入了 langchain4j-spring-boot-starter，框架启动时就会自动扫描你项目里的所有 AI 接口，并自动帮你执行那个 AiServices.create(...) 的操作。它后面的逻辑其实是
> // 这是 LangChain4j 在内存里帮你生成的伪代码，你看不见，但它在运行
> class AiCodeGeneratorService_Impl_By_LangChain4j implements AiCodeGeneratorService {
> 
>     private ChatModel chatModel; // 它把 chatModel 塞进去了
> 
>     @Override
>     public HtmlCodeResult generateHtmlCode(String userMessage) {
>         // 1. 读取接口上的 @SystemMessage 注解
>         String systemPrompt = readSystemMessageAnnotation(); 
>         
>         // 2. 把系统提示词 + 用户传入的 userMessage 拼起来
>         String finalPrompt = systemPrompt + "\n用户说：" + userMessage;
>         
>         // 3. 调用真正的 AI 模型 (DeepSeek)
>         String aiRawResponse = chatModel.generate(finalPrompt);
>         
>         // 4. 因为返回值是 HtmlCodeResult，它会自动把 aiRawResponse (JSON) 转成对象
>         HtmlCodeResult result = JsonUtil.parse(aiRawResponse, HtmlCodeResult.class);
>         
>         // 5. 返回结果
>         return result;
>     }
> }
> 还有个问题就是，既然约定大于配置我AiCodeGeneratorService就能运行，那你多此一举干嘛？
> 回答：它就是帮你把 AI 服务变成 Spring 可以管理的 Bean，让你在任何地方都能 @Resource 直接用！
>     不写这个工厂类，你的测试类能跑吗？跑不了！绝对跑不了！
> 而且你测试类
>     @Resource
>     private AiCodeGeneratorService aiCodeGeneratorService;
> 问题：你凭什么能@Resource啊？
> public interface AiCodeGeneratorService {
> 
>     @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
>     HtmlCodeResult generateHtmlCode(String userMessage);
> }
> 你这个明明是个接口而且你没写@service什么的，你根本没标识它是组件啊，你怎么能@Resource呢？
> 
> 回答：不是接口变成了 Bean是【工厂类里的 @Bean 方法】把它变成了 Bean！
> 1. AiCodeGeneratorService 接口 → 不是Bean
> 2. 工厂类 @Bean 方法 → 创建一个实例
> 3. 实例 → 扔进 Spring 容器
> 4. 你写 @Resource → Spring 从容器拿给你
> ```