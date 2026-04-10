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
9. 

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