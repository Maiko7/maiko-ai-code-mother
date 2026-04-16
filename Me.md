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
17. 你别管这个设计模式那个设计模式 本质就是封装，就是为了可扩展不修改原有代码。这就是最本质的，你写的代码有这个效果就行了。你别管那些七七八八的模式。
18. 为什么user的时候没有让AI直接开发，而app的时候让AI开发。因为这个时候已经有参照了，可以让AI参照user来生成app。
19. 为什么用UserService而不是UserMapper呢？如果你的服务要调用其他的服务，建议还是用UserService，尤其这个项目后期要变成微服务，微服务也是一个服务调用另一个服务而不是调用另一个服务里的代码。
20. 比如你在78行打断点 你不是老是按F9（Resume Program）就是![image-20260415071416324](C:\Users\73450\AppData\Roaming\Typora\typora-user-images\image-20260415071416324.png)这个的时候就马上跳到下一个断点去了，那你这个时候想在78行断点的下一行，难道你一路打断点下去？其实不需要。你只需要按F8（Step Over)即![image-20260415071459309](C:\Users\73450\AppData\Roaming\Typora\typora-user-images\image-20260415071459309.png)，至于F7（Step Into)和Shift+F8（Step Out)就是。
21. 你下次比如你新建一个项目你有思路了的时候，你可以这样，先写1 2 3 4把你的步骤写出来注释出来 然后告诉大模型说我有hutool工具包我有什么包，然后让大模型生成即可。你不用一整个项目直接生成，你可以这样慢慢来。先要有思路。
22. 你会发现鱼皮经常会把就是重要的流程可能出问题的地方try catch一下防止程序异常挂机。

    ### 1. Step into（单步进入 / 步入)

    - 执行**当前这一行**
    - 如果这一行**调用了一个函数 / 方法**，会**跳进这个函数内部**，继续一行一行执行
    - 适合：想仔细看函数内部是怎么跑的

    ### 2. Step out（单步跳出 / 步出）

    - 直接**把当前正在执行的函数跑完**
    - 然后**跳出这个函数**，回到上一层调用它的地方
    - 适合：不想再看函数内部细节，想快速回到外层

    ------

    ### 简单对比

    - **Step over（单步跳过）**：不进函数，直接执行完这一行
    - **Step into**：进函数，一行一行看
    - **Step out**：从函数里直接跳出来
23. 

    ## 一、先把 `my-extension` 分支的代码提交好

    确保你在 `my-extension` 分支（IDEA 左下角当前分支显示 `my-extension`），执行：

    ```bash
    git add .
    git commit -m "feat: 完成AI流式生成、多角色权限、封号拦截等核心功能"
    ```

    （提交信息按你实际改的内容写，规范就好）

    ------

    ## 二、切换到 `master` 主分支

    ### 方法 1（IDEA 图形化，最省事）

    直接点 IDEA 右下角的分支名 → 选择 `master` → 点击 **Checkout**，自动切换。

    ### 方法 2（命令行）

    ```bash
    git checkout master
    ```

    > 切换前确保 `my-extension` 分支代码已提交，否则会有未提交文件冲突。

    ------

    ## 三、把 `my-extension` 合并到 `master`

    ### 方法 1（IDEA 图形化，推荐）

    1. 切换到 `master` 后，右下角点分支名 → 找到 `my-extension` → 选择 **Merge into Current**
    2. 弹出窗口直接点 **Merge**，如果没有冲突，瞬间合并完成。

    ### 方法 2（命令行，100% 通用）

    ```bash
    git merge my-extension
    ```

    - 无冲突：直接合并成功，`master` 分支就有了 `my-extension` 的所有更新
    - 有冲突：IDEA 会弹出冲突窗口，手动解决后点 **Merge** 即可

    ------

    ## 四、合并完，推送到 Gitee + GitHub（按你要求先 Gitee 再 GitHub）

    ### 1. 先推 Gitee（你的 `origin` 远程仓库）

    ```bash
    git push origin master
    ```

    ### 2. 再推 GitHub（你的 `github` 远程仓库）

    ```bash
    git push github master
    ```

    ------

    ## 五、可选：合并后删除 `my-extension` 分支（不删也不影响）

    如果这个功能分支用完了，本地可以删掉：

    ```bash
    git branch -d my-extension
    ```

    ```bash
    git push origin --delete my-extension
    git push github --delete my-extension
    ```

24.  Manager的作用是通用的处理层，不和任何业务逻辑绑定（这就是与common的区别，你common是通用的但是你与业务逻辑绑定），专门用来对接第三方资源的，比如说和对象存储进行交互，比如BI项目中的AImanager。通用的独立于业务的处理层。

25. 你看到全局复用实例的时候，一定要想想会不会有多线程的危险。比如下面的webDriver假设有两个线程，第一个截图page1第二个截图page2最终可能会导致第一个请求截错了，截成了page2，因为假设第一个线程进来它打开的是page1，这个时候第二个线程进来了，由于用的是同一个WebDriver有可能之前打开的页面被覆盖掉了。变成了page2。

    ```java
    // 危险：多线程共享同一个driver
    private static final WebDriver webDriver = new ChromeDriver();
    
    // 线程A: driver.get("page1.html") -> 截图
    // 线程B: driver.get("page2.html") -> 截图  
    // 结果：线程 A 可能截到 page2 的内容
    ```

26. 你看鱼皮截图服务优化，提供了四种思路。第一种：每次创建新实例、第二种：连接池。维护一个WebDriver池，按需分配和回收。第三种：ThreadLocal模式，每个线程使用同一个WebDriver。第四种：使用队列，将要执行的截图任务依次放到队列中，WebDriver线程组依次取出任务执行，本质并行变串行。那现在他不是说乱用一种，你现在你不能以学生的思维我会哪个我用哪个，而是要根据实际情况来调整，你并发量不大的情况下选择ThreadLocal稳不会出现线程问题但是性能差。假设这时候你的并发量上来了你就可以考虑不用ThreadLocal而是线程池了。

>  **🔍 深度解析：什么时候用哪种？**
>
> | 方案           | 形象比喻                           | 适用场景                                                  | 优点                               | 缺点                          |
> | -------------- | ---------------------------------- | --------------------------------------------------------- | ---------------------------------- | ----------------------------- |
> | 1. 每次新建    | 买一次性筷子 用完就扔              | 极低频任务 (如：每天只跑一次的报表)                       | 代码最简单 绝对线程安全            | 性能极差 资源浪费严重         |
> | 2. 连接池      | 共享单车/出租车 借来用，用完还回去 | 高频并发的核心业务 (如：电商秒杀、用户截图)               | 性能最强 可控并发量 资源复用率高   | 实现最复杂 需要引入池化库     |
> | 3. ThreadLocal | 专属配枪 专人专用，离职归还        | 中低频，但要求速度快 (如：后台管理系统、偶尔的管理员操作) | 线程安全 无需同步锁 第二次调用快   | 内存占用高 容易内存溢出 (OOM) |
> | 4. 队列模式    | 医院排队叫号 只有一个医生看病      | 资源极其昂贵且不支持并发 (如：老旧系统、硬件限制)         | 保护下游服务 削峰填谷 不会撑爆内存 | 吞吐量低 用户等待时间长       |
>
> #### **方案一：每次创建新实例**
>
> - 什么时候用：
>   - 你的任务**几天才运行一次**。
>   - 你在写单元测试。
>   - 你完全不在乎那几百毫秒的启动时间。
> - 千万别用：
>   - 任何用户端的功能（比如用户点一下就要出结果）。因为 Chrome 启动真的很慢，用户会以为网站卡死了。
>
> #### **方案二：连接池 —— 【大厂/高并发首选】**
>
> - 什么时候用：
>   - **这是生产环境的最佳实践。**
>   - 当你的网站有**很多人同时访问**（比如每秒几十上百个请求）。
>   - 当你需要严格控制资源时（比如服务器内存只有 8G，我最多只能开 5 个浏览器，多了就排队）。
> - 怎么做：
>   - 使用像 `commons-pool2` 这样的库，或者 Selenium 专门的池化实现。
>   - 配置 `maxTotal`（最大连接数）和 `maxIdle`（最大空闲时间）。
>
> #### **方案三：ThreadLocal —— 【开发便捷/中低频首选】**
>
> - 什么时候用：
>   - 当你想要**高性能**，但不想写复杂的池化管理代码时。
>   - 当你的并发量**不大**（比如公司内部用的工具，几个人同时点没关系）。
>   - 当你同一个线程内需要多次调用该资源时（第一次初始化，后面直接复用，飞快）。
> - 致命陷阱：
>   - **必须小心内存泄漏！** 如果你的线程是“虚拟线程”或者频繁创建销毁的，ThreadLocal 里的东西如果不及时 `remove()`，内存马上爆满。
>   - **不适合海量并发**：1000 个用户 = 1000 个浏览器 = 服务器崩溃。
>
> #### **方案四：队列模式 —— 【保底/限流策略】**
>
> - 什么时候用：
>   - 当你的资源**实在太贵**，或者第三方服务**限制了并发数**（比如买了个廉价 API，规定每秒只能调 1 次）。
>   - 当你宁愿让用户等一会儿，也不能让服务器挂掉的时候。
> - 怎么做：
>   - 搞一个单例的消费者（Consumer），一直循环从队列里取任务。
>   - 用户的请求只是往队列里 `put` 一个任务，然后前端轮询结果。
>
> ------
>
> ### **💡 给你的一句话建议**
>
> - **如果是做毕设、小项目、内部工具：**
>   👉 选 **ThreadLocal**（记得加超时清理）或者 **每次新建**（如果真的很慢就别用这个）。因为它代码最好写，不用引入额外的池化库。
> - **如果是做商业项目、SaaS 平台、要上线赚钱的系统：**
>   👉 必须选 **连接池**。虽然写起来麻烦点，但它能帮你扛住流量，还能防止服务器被几个异常请求搞挂。
> - **如果是处理那种超大的批量任务（比如半夜帮客户生成 1 万份报告）：**
>   👉 选 **队列模式**。慢慢跑，别把机器累死。

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

10. ```
    git push origin master
    git push github master
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

---





> ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
> 为什么他说如果不加<=0 攻击者一直拿小于0的appId攻击数据库 这什么意思？这个说法形象地描述了一种**“无效参数攻击”**或**“资源消耗攻击”**。
>
> 简单来说，攻击者利用你代码逻辑上的小漏洞，通过发送大量“看似合法但实际无效”的请求，让你的服务器和数据库做无用功，最终导致系统变慢甚至崩溃。
>
> 下面我为你拆解一下这背后的原理：
>
> ### **1. 为什么** `null` **校验不够，必须校验** `<= 0`**？**
>
> 你可能会觉得：“`appId` 是 `Long` 类型，如果前端传个 `-1` 或者 `0`，数据库里反正也查不到数据，查出来是 `null` 不就行了吗？为什么要专门拦截？”
>
> **问题就出在“查不到”这个过程上。**
>
> 即使参数是 `-1`，你的程序依然会照常运行：
>
> 1. **Controller 层**：接收请求。
> 2. **Service 层**：接收指令，准备干活。
> 3. **DAO/Mapper 层**：向数据库发送 SQL 语句，比如 `SELECT * FROM app WHERE id = -1`。
> 4. **数据库**：收到 SQL，开始解析，去索引里查找，发现没有这条记录，返回空结果。
> 5. **Service 层**：拿到空结果，判断 `app == null`，然后抛出“应用不存在”的异常。
>
> **你看，虽然结果是“查不到”，但你的数据库、连接池、CPU 都已经为了这个无效请求“跑了一整套流程”。**
>
> ### **2. 攻击者是如何“攻击”的？**
>
> 假设你没有加 `appId <= 0` 的校验，攻击者写了一个脚本，在一秒钟内向你的接口发送 **10,000 次** 请求，参数全是 `appId = -1`。
>
> - **你的服务器**：接收了 10,000 次请求。
>
> - **你的数据库**：被迫执行了 10,000 次 `SELECT * FROM app WHERE id = -1`。
>
> - 后果
>
>   ：
>
>   - **CPU 飙升**：数据库和处理请求的线程都在空转。
>   - **连接池耗尽**：数据库连接池里的连接被这些无效请求占满，导致正常用户的请求（比如 `appId = 100`）拿不到连接，页面转圈圈。
>   - **拒绝服务**：系统资源被耗尽，正常用户无法访问，这就达到了攻击者的目的。
>
> 这就是所谓的“攻击数据库”——**不是把你的数据删了，而是用垃圾请求把你的数据库“累死”或“堵死”。**
>
> ### **3. 为什么 Controller 层和 Service 层都要校验？（纵深防御）**
>
> 你可能会问：“Service 层里不是也有校验吗？为什么 Controller 层还要再来一次？”
>
> 这是一种**“纵深防御”**的安全思想。
>
> - **Controller 层（第一道防线）**：
>   - **职责**：快速拦截。
>   - **作用**：在请求进入复杂的业务逻辑之前，就把明显的垃圾参数（如 `null`、`<=0`）拦截下来。
>   - **好处**：节省资源。如果在这里就报错，Service 层、DAO 层、数据库都不用启动了，直接返回 `400 Bad Request`。
> - **Service 层（第二道防线）**：
>   - **职责**：业务逻辑完整性校验。
>   - **作用**：防止 Controller 层漏网之鱼，或者防止其他 Controller 调用该 Service 时传入非法参数。
>   - **好处**：确保业务逻辑的绝对安全。
>
> **总结：**
> Controller 层的校验是为了**“快”**（尽早拒绝，节省性能），Service 层的校验是为了**“稳”**（保证业务逻辑在任何情况下都不出错）。两者结合，系统才更安全、更高效。
>
> 以后见到Long类型都要注意 进行一个校验<=0



---



这是循环依赖的问题，Spring不知道先有appServiceImpl还是chatHisttoryServiceImpl，所以你让一个后加载退一步即可。

```java
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    @Lazy
    private AppService appService;
```

![image-20260413080554373](C:\Users\73450\AppData\Roaming\Typora\typora-user-images\image-20260413080554373.png)



----



> package com.maiko.maikoaicodemother.core.saver;
>
> import cn.hutool.core.io.FileUtil;
> import cn.hutool.core.util.StrUtil;
> import com.maiko.maikoaicodemother.constant.AppConstant;
> import com.maiko.maikoaicodemother.exception.BusinessException;
> import com.maiko.maikoaicodemother.exception.ErrorCode;
> import com.maiko.maikoaicodemother.model.enums.CodeGenTypeEnum;
>
> import java.io.File;
> import java.nio.charset.StandardCharsets;
>
> /**
>  * 【类定义】抽象代码文件保存器
>  *
>  * 设计模式：模板方法模式
>  * 作用：定义了一套保存代码文件的“标准流程”（saveCode），
>  *      但是把流程中具体的“细节”（比如存什么文件、目录叫什么）留给子类去实现。
>  *
>  * <T>：泛型，代表不同的代码结果对象（比如 HtmlCodeResult, JavaCodeResult 等）。
>  */
>    public abstract class CodeFileSaverTemplate<T> {
>
>     // 【常量】定义所有文件保存的根目录，从常量类中读取
>     protected static final String FILE_SAVE_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;
>
>     /**
>      * 【核心流程】模板方法
>        *
>      * final 关键字：表示这个方法是“最终”的，子类不能修改（重写）这个流程。
>      * 保证所有代码保存的步骤都是一致的：校验 -> 建目录 -> 保存 -> 返回。
>        */
>          public final File saveCode(T result, Long appId) {
>         // 1. 验证输入：检查数据是否合法
>         validateInput(result);
>         // 2. 构建唯一目录：根据应用ID创建专属文件夹
>         String baseDirPath = buildUniqueDir(appId);
>         // 3. 保存文件：这是抽象方法，具体怎么存（存几个文件、叫什么名）由子类决定
>         saveFiles(result, baseDirPath);
>         // 4. 返回结果：返回创建好的目录文件对象
>         return new File(baseDirPath);
>          }
>
>     /**
>      * 【步骤1：校验】
>      * protected：允许子类访问，甚至允许子类重写（扩展）校验逻辑。
>      * 默认只检查对象是否为空，子类（如 HtmlCodeFileSaverTemplate）可以增加更多检查。
>        */
>          protected void validateInput(T result) {
>         if (result == null) {
>             throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码结果对象不能为空");
>         }
>          }
>
>     /**
>      * 【步骤2：建目录】
>      * final：子类不能修改建目录的逻辑。
>      * 逻辑：根目录 + 类型_应用ID（例如：output/html_101）。
>        */
>          protected final String buildUniqueDir(Long AppId) {
>         if (AppId == null) {
>             throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用ID不能为空");
>         }
>         // 获取代码类型（例如 "html"），这是调用子类实现的 getCodeType()
>         String codeType = getCodeType().getValue();
>         // 拼接目录名：类型_应用ID
>         String uniqueDirName = StrUtil.format("{}_{}", codeType, AppId);
>         // 拼接完整路径
>         String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
>         // 创建目录（如果不存在）
>         FileUtil.mkdir(dirPath);
>         return dirPath;
>          }
>
>     /**
>      * 【工具方法】写入单个文件
>      * final：工具方法，逻辑固定，子类直接调用即可。
>      * 作用：把字符串内容写入到指定目录下的指定文件中。
>        */
>          protected final void writeToFile(String dirPath, String filename, String content) {
>         // 只有内容不为空时才写入
>         if (StrUtil.isNotBlank(content)) {
>             String filePath = dirPath + File.separator + filename;
>             // 使用 Hutool 工具类写入文件，指定 UTF-8 编码
>             FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
>         }
>          }
>
>     /**
>      * 【抽象方法1】获取代码类型
>      * abstract：没有方法体，强制要求子类必须实现。
>      * 目的：让父类知道当前处理的是 HTML 还是 Java 代码，用于创建目录名。
>        */
>          protected abstract CodeGenTypeEnum getCodeType();
>
>     /**
>      * 【抽象方法2】保存文件的具体实现
>      * abstract：强制子类实现。
>      * 目的：父类不知道具体要存几个文件、文件名是什么，完全交给子类（如 HtmlCodeFileSaverTemplate）去写。
>        */
>        protected abstract void saveFiles(T result, String baseDirPath);
>          }
>
>
> package com.maiko.maikoaicodemother.core.saver;
>
> import cn.hutool.core.util.StrUtil;
> import com.maiko.maikoaicodemother.ai.model.MultiFileCodeResult;
> import com.maiko.maikoaicodemother.exception.BusinessException;
> import com.maiko.maikoaicodemother.exception.ErrorCode;
> import com.maiko.maikoaicodemother.model.enums.CodeGenTypeEnum;
>
> /**
>  * 【类定义】多文件代码保存器
>  *
>  * 作用：专门用来保存包含多个文件的项目（例如：HTML + CSS + JS）。
>  * 设计思路：
>  *   它继承自父类 CodeFileSaverTemplate，复用了“建目录、校验、写文件”的通用流程。
>  *   它只需要告诉父类：“我是多文件类型”以及“具体要存哪几个文件”。
>  *   正如注释所说，这种拆分方式让代码结构非常清晰，维护时只需要找对应的类即可。
>    */
>     public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {
>
>     /**
>      * 【实现1：定义类型】
>      * 告诉系统：我是处理多文件类型的。
>        */
>         @Override
>         public CodeGenTypeEnum getCodeType() {
>         return CodeGenTypeEnum.MULTI_FILE;
>         }
>
>     /**
>      * 【实现2：具体保存逻辑】
>      * 这里定义了多文件项目具体包含哪些文件。
>        *
>      * 逻辑：
>      *   1. 调用父类的 writeToFile 方法。
>      *   2. 分别把 HTML、CSS、JS 内容写入对应的文件名（index.html, style.css, script.js）。
>      *   3. 如果某个内容为空（比如 CSS），writeToFile 内部会自动跳过（基于之前的代码逻辑）。
>           */
>           @Override
>           protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
>             // 保存 HTML 文件 -> index.html
>             writeToFile(baseDirPath, "index.html", result.getHtmlCode());
>             // 保存 CSS 文件 -> style.css
>             writeToFile(baseDirPath, "style.css", result.getCssCode());
>             // 保存 JavaScript 文件 -> script.js
>             writeToFile(baseDirPath, "script.js", result.getJsCode());
>           }
>
>     /**
>      * 【实现3：自定义校验】
>      * 针对多文件场景的特殊校验规则。
>        */
>         @Override
>         protected void validateInput(MultiFileCodeResult result) {
>         // 1. 先执行父类的通用校验（比如检查 result 是否为空）
>         super.validateInput(result);
>
>         // 2. 增加特定业务校验：
>         // 对于多文件项目，CSS 和 JS 可以为空（可能只有结构），但 HTML 必须有。
>         if (StrUtil.isBlank(result.getHtmlCode())) {
>             throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
>         }
>         }
>          }
>
>  private static final MultiFileCodeFileSaverTemplate multiFileCodeFileSaver = new MultiFileCodeFileSaverTemplate();
>
> 它的MultiFileCodeFileSaverTemplate没定义 // 1. 验证输入：检查数据是否合法
>         validateInput(result);
>         // 2. 构建唯一目录：根据应用ID创建专属文件夹
>         String baseDirPath = buildUniqueDir(appId);
>         // 3. 保存文件：这是抽象方法，具体怎么存（存几个文件、叫什么名）由子类决定
>         saveFiles(result, baseDirPath);
>         // 4. 返回结果：返回创建好的目录文件对象
>         return new File(baseDirPath);
> 它会执行这些吗？ 我看MultiFileCodeFileSaverTemplate只有saveFilesvalidateInput？？
>
> 
>
> 
>
> 这正是**模板方法模式**最精妙的地方。答案是肯定的：**它绝对会执行这些代码**。
>
> 虽然 `MultiFileCodeFileSaverTemplate` 类里看起来只写了 `saveFiles` 和 `validateInput`，但它通过 `extends CodeFileSaverTemplate` **继承**了父类所有的非私有方法。
>
> 这里有一个关键的运行机制，我来为你拆解一下：
>
> ### **核心机制：继承与多态**
>
> 当你调用 `multiFileCodeFileSaver.saveCode(...)` 时，程序执行的流程如下：
>
> #### **1. 入口在父类**
>
> `saveCode` 方法定义在父类 `CodeFileSaverTemplate` 中，并且被 `final` 修饰。
>
> - **结果**：子类无法重写它，所以当你调用这个方法时，**永远执行的是父类里定义的那套标准流程**。
>
> #### **2. 流程中的“回调”**
>
> 在父类的 `saveCode` 流程中，它调用了 `validateInput(result)` 和 `saveFiles(result, baseDirPath)`。
>
> - 虽然代码写在父类里，但这两个方法是 `protected` 且非 `final` 的（甚至是抽象的）。
> - **Java 的多态机制**会判断：**“当前这个对象实际上是 `MultiFileCodeFileSaverTemplate` 的实例”**。
> - **结果**：父类在运行时会自动去调用**子类重写后的版本**。
>
> ### **图解执行流程**
>
> 假设你执行了这行代码：
>
> ```java
> // 调用入口
> File dir = multiFileCodeFileSaver.saveCode(result, 101L);
> ```
>
> **实际发生的步骤（跳来跳去的过程）：**
>
> 1. 进入父类`saveCode()`：
>    - 执行 `validateInput(result)` -> **跳转到子类**的 `validateInput`（执行特定校验）。
>    - 执行 `buildUniqueDir(appId)` -> **留在父类**（执行创建目录逻辑）。
>    - 执行 `saveFiles(result, baseDirPath)` -> **跳转到子类**的 `saveFiles`（执行具体的 HTML/CSS/JS 写入）。
>    - 执行 `return new File(baseDirPath)` -> **留在父类**（返回结果）。
>
> ### **为什么这样设计？**
>
> 如果不使用这种模式，你可能需要在 `MultiFileCodeFileSaverTemplate` 里把 `saveCode` 复制粘贴一遍，那样如果有 10 个不同的保存器，改一个公共逻辑（比如目录命名规则）就要改 10 次。
>
> **现在的写法：**
>
> - **父类**负责“骨架”（怎么做饭：买菜 -> 洗菜 -> 炒菜）。
> - **子类**负责“细节”（做什么饭：洗什么菜、炒什么菜）。
>
> 所以，尽管子类代码看起来很少，它依然完整拥有了父类定义的所有执行步骤。



----



> 为什么我
>
> ```java
> @Slf4j
> @SpringBootTest
> public class WebScreenshotUtilsTest {
> 
>     @Test
>     void saveWebPageScreenshot() {
>         String testUrl = "https://blog.csdn.net/weixin_44146541?type=blog";
>         String webPageScreenshot = WebScreenshotUtils.saveWebPageScreenshot(testUrl);
>         Assertions.assertNotNull(webPageScreenshot);
>     }
> }
> ```
>
> Q：执行这个测试类，它断点跑到了AiCodeGeneratorServiceFactory里的createAiCodeGeneratorService里面去了？我的WebScreenshotUtils根本都没有用到AiCodeGeneratorServiceFactory。我只是访问网页截图呀。
>
> A：这是一个非常典型的 **Spring 上下文加载** 问题。
>
> 你运行的是 `WebScreenshotUtilsTest`，但它却跳进了 `createAiCodeGeneratorService`（一个看起来是配置类里的 Bean 创建方法），这说明：**你写的测试类并不是一个孤立的单元测试，而是启动了整个 Spring Boot 应用上下文。**
>
> 这种情况通常由以下三个原因导致：
>
> ### **1. 测试类上加了 Spring 的注解**
>
> 这是最常见的原因。
> 如果你的测试类顶部使用了 Spring Test 的注解，JUnit 就会启动 Spring 容器，加载所有 Bean，而不仅仅是运行你的那个方法。
>
> - 检查你的测试类是否有以下注解：
>
>   ```java
>   @SpringBootTest // 如果有这个，它会加载整个应用的配置
>   @ContextConfiguration // 如果有这个，它也会加载配置类
>   @RunWith(SpringRunner.class) // JUnit 4 风格，也会启动上下文
>   ```
>
> - **为什么会断在这里？**
>   `createAiCodeGeneratorService` 看起来是一个 `@Bean` 工厂方法。当 Spring 启动时，它会扫描所有配置类，并执行 `@Bean` 方法来创建对象放入容器。如果这个 Bean 是单例的（默认），或者被其他组件依赖，Spring 在启动阶段就会调用这个方法。
>   **你的测试代码还没执行 `saveWebPageScreenshot`，Spring 容器初始化就已经跑到了你的断点上。**



----

> ```java
>  try {
>             // 2. 上传到对象存储
>             String cosUrl = uploadScreenshotToCos(localScreenshotPath);
>             ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "截图上传对象存储失败");
>             log.info("网页截图生成并上传成功: {} -> {}", webUrl, cosUrl);
>             return cosUrl;
>         } finally {
>             // 3. 清理本地文件
>             cleanupLocalFile(localScreenshotPath);
>         }
> ```
>
> 为什么他说要清理本地文件？防止本地文件生成后用完把它清理掉要不然会导致资源泄露
>
> 什么意思？怎么导致资源泄露？
>
> 这里的“资源泄露”指的不是内存溢出，而是**服务器硬盘空间被占满（磁盘空间耗尽）**。
>
> 你可以把服务器的硬盘想象成一个**只有 50GB 空间的仓库**。
>
> ### **💥 为什么会“泄露”？**
>
> 如果不写 `cleanupLocalFile`（清理本地文件），流程是这样的：
>
> 1. **用户 A** 来了，生成了一张截图，占用了 **1MB** 硬盘。
> 2. **用户 B** 来了，又生成一张，占用 **1MB**。
> 3. ...
> 4. **一万个用户** 来了之后，你的服务器上就留下了 **10,000 张没用的垃圾图片**。
>
> 虽然你把图片上传到了云端（COS），但**本地的这些副本并没有自动删除**。它们就像你买完东西后，把快递盒子、泡沫塑料全都堆在客厅里。刚开始没事，但日子久了，**你的房子（硬盘）会被这些垃圾彻底塞满**。
>
> **你这就是典型的没有上线的思维，你还停留在小打小闹，你想如果大量的打进去，你的本地扛得住吗？**



---



>   Thread.startVirtualThread(() 
> Thread.ofVirtual()
> Q：怎么一会是那个一会是这个？
>
> A：这俩其实是一回事，都是用来**启动虚拟线程**的，只不过一个是**“快捷方式”**，另一个是**“高级定制版”**。
>
> 你可以把它们的关系理解为 **“普通构造函数”** 和 **“建造者模式”** 的区别：
>
> ### **⚡️** `Thread.startVirtualThread(Runnable)` **—— “快餐模式”**
>
> - **特点**：简单、直接、一步到位。
> - **作用**：创建一个虚拟线程，并**立刻**开始运行。
> - **适用场景**：你不需要给线程起名字，也不需要设置优先级，只想赶紧把活儿扔给后台去干（就像刚才的截图任务）。
>
> ```java
> // 写法：一行搞定
> Thread.startVirtualThread(() -> {
>     System.out.println("我在后台干活");
> });
> ```
>
> ------
>
> ### **🛠️** `Thread.ofVirtual()` **—— “定制模式”**
>
> - **特点**：灵活、可配置。
> - **作用**：它是**建造者**。你先配置参数（比如起个名字），最后调用 `.start()` 才会真正启动。
> - **适用场景**：你需要调试，想给线程起个好听的名字（比如叫 "Screenshot-Worker"），或者你想先创建线程对象，过一会儿再启动它。
>
> ```java
> // 写法：分三步走
> Thread.ofVirtual()        // 1. 声明我要一个虚拟线程
>       .name("my-worker")  // 2. (可选) 配置参数，比如起个名
>       .start(() -> {      // 3. 最后才启动
>           System.out.println("我是定制版的虚拟线程");
>       });
> ```
>
> ### **📌 总结**
>
> - 如果你只是想**“赶紧异步执行”**，用第一个 `Thread.startVirtualThread(...)` 就够了，代码最清爽。
> - 如果你想**“给线程起个名方便看日志”**，就用第二个 `Thread.ofVirtual().name(...).start(...)`。



---



> 他为什么说你如果在我们的场景下你如果还把它在每个请求都释放，是不是又跟我每次调用都获取一样了，所以你在我们这个项目中就是不应该释放webdriver，有可能你的内存容量会超限，缺点是如果你的线程数较多，webdriver也较多，一直不释放可能导致内存溢出。这句话什么意思我理解不了。
>
> 这句话其实是在吐槽：**你刚才那个“用完即焚”的写法，虽然安全了，但把 ThreadLocal 的优势给浪费了，变成了“每次都要重新造轮子”。**
>
> 咱们把它拆解成三个层次来理解，你就能明白为什么他说“不应该释放”以及“内存溢出”是怎么回事。
>
> ### **🎬 场景还原：你在做什么？**
>
> 你的业务场景是 `generateAppScreenshotAsync`（异步生成截图）。
> Spring Boot 处理这个请求时，会分配一个**线程**。
>
> #### **你的代码逻辑（用完即焚）**
>
> 1. **线程来了** -> `getDriver()` -> 发现没有 -> **启动浏览器**（耗时 2秒）。
> 2. **截图** -> 上传。
> 3. **finally 块** -> `removeDriver()` -> **关闭浏览器**。
> 4. **线程结束**。
>
> #### **吐槽点在哪里？**
>
> 正如那个人所说：“跟我每次调用都获取一样了”。
> 因为你每次都在 `finally` 里把它杀掉了。下一次如果有请求来，你还得重新花 2 秒钟去启动浏览器。
> **ThreadLocal 的本意是“存住它，下次接着用”，结果你把它当成了“临时变量”用。**
>
> ------
>
> ### **💡 他说的“不应该释放”是什么意思？**
>
> 他的意思是：**既然用了 ThreadLocal，就应该让它真正“驻留”在内存里，供同一个线程反复使用。**
>
> 想象一下，如果你的服务器运行了很久，某个核心工作线程处理了 1000 个截图任务：
>
> - 你的写法（释放版）
>
>   ：
>
>   - 启动浏览器 x 1000 次。
>   - 总耗时：1000 * 2秒 = 2000秒（浪费生命）。
>
> - 他的理想写法（不释放版）
>
>   ：
>
>   - 第 1 次：启动浏览器（耗时 2秒）。
>   - 第 2~1000 次：直接拿现成的浏览器用（耗时 0秒）。
>   - **只有当线程销毁时，才关闭浏览器。**
>
> **这就是他说的“不应该释放”：** 不要在这个方法内部就杀掉浏览器，应该让它活着，等着下一个任务来复用。
>
> ------
>
> ### **⚠️ 那为什么又说“会导致内存溢出”？**
>
> 这就涉及到了硬币的另一面。如果你听他的话，“一直不释放”，会发生什么？
>
> 假设你的网站很火，一瞬间来了 **500 个用户** 同时生成截图。
> Spring Boot 可能会开启 **500 个线程** 来处理这些请求。
>
> - 如果不释放（且没有数量限制）：
>   - 线程 1 创建了一个 Chrome 浏览器（占用 300MB 内存）。
>   - 线程 2 创建了一个 Chrome 浏览器（占用 300MB 内存）。
>   - ...
>   - 线程 500 创建了第 500 个 Chrome 浏览器。
>   - **总内存消耗**：500 * 300MB ≈ **150 GB**！💥 **服务器直接爆炸（OOM）。**
>
> Chrome 浏览器是非常吃内存的怪物。
>
> ------
>
> ### **📌 总结：这是一个两难的困境**
>
> 那个人其实指出了 **ThreadLocal 方案在这个特定场景下的尴尬之处**：
>
> - **如果你每次都用完关掉**：那就失去了复用的意义，每次都要重启浏览器，慢得要死。（这是你现在的代码）
> - **如果你一直不关**：并发量一上来，几百个浏览器同时开着，内存瞬间爆满。（这是他警告的风险）
>
> ### **🛠️ 真正的最佳解决方案是什么？**
>
> 既然“单例模式”不安全（串号），“ThreadLocal 模式”太重（吃内存），那么工业界的标准答案其实是 **连接池**。
>
> 你需要的是一个 **WebDriver 池**。
>
> #### **工作原理**
>
> 1. 池子里预先放好 5 个浏览器实例。
> 2. 请求来了，从池子里**借**一个。
> 3. 用完之后，**还**回池子里（而不是杀掉，也不是占着不走）。
> 4. 如果池子空了，请求就排队等待。
>
> 这样既保证了**安全**（一次只给一个人用），又保证了**性能**（不用反复重启），还控制了**内存**（最多只有 5 个浏览器）。
>
> ### **📝 给你的建议**
>
> 针对你这个项目，如果并发量不是特别大（比如每秒几十个请求），目前的 **ThreadLocal + 用完即焚** 其实是可以接受的，虽然慢点，但至少稳。
>
> 如果你想优化，可以试着把 `removeDriver()` 去掉，看看内存占用情况。如果发现内存飙升，就得引入 **对象池** 了。



---



>   // 【封面图兜底逻辑】如果 cover 字段为空，使用默认封面
>
> ```java
>     if (StrUtil.isBlank(app.getCover())) {
> 
>         appVO.setCover(AppConstant.DEFAULT_COVER_URL);
> 
>         log.debug("应用 {} 的封面为空，使用默认封面: {}", app.getId(), AppConstant.DEFAULT_COVER_URL);
> 
>     }
> ```
>
>    
>
> ```java
> /**默认应用封面图路径（静态资源）
>  当应用的 cover 字段为空或截图失败时，使用此默认封面
>      */
>     String DEFAULT_COVER_URL = "/images/Mango.png";
> ```
>
> 你这个常量就写个这样的地址它能访问到？ 你不写比如E:\Code\Maiko7\AI\maiko-ai-code-mother\src\main\resources\static\images\Mango.png？
>
> 
>
> **不用写绝对路径，直接用 `/images/Mango.png` 是对的！**
>
> 如果写成 `E:\Code\...\Mango.png`，那只能在你自己的电脑上访问，部署到服务器（Linux）上就彻底挂了。
>
> **原因如下：**
>
> 1. **Spring Boot 的默认规则**：
>    Spring Boot 默认会把 `src/main/resources/static` 目录下的文件当作**静态资源**根目录。
>    - 物理路径：`src/main/resources/static/images/Mango.png`
>    - 访问路径（URL）：`http://你的域名/images/Mango.png`
> 2. **数据库存的是什么？**
>    数据库存的应该是**“网址”**，而不是**“硬盘文件路径”**。前端拿到这个字符串是直接放在 `<img src="...">` 里用的，它只认识网址。



---



> // 【资源清理1】先查询应用信息，获取云端封面文件的 key
> App app = this.getById(appId);
> if (app != null && StrUtil.isNotBlank(app.getCover())) {
>     deleteCoverFromCos(app.getCover());
> }
>
> // 【资源清理2】删除关联的对话历史
> chatHistoryService.deleteByAppId(appId);
>
> // 删除应用
> super.removeById(id);
> Q：他这个直接删除应用不就行了？干嘛还要删除资源1资源2？
>
> A：这其实是为了解决“数据删了，垃圾还在”的问题。
>
> 如果只执行 `super.removeById(id)`，虽然数据库里的记录没了，但应用产生的关联文件和历史数据并不会自动消失，时间久了就会造成**存储浪费**和**脏数据**。
>
> 具体来看，这两步清理工作主要解决的是以下隐患：
>
> ### **🗑️ 为什么要删除资源1（云端封面图）？**
>
> - 省钱（存储费）：
>   - 数据库里的记录只是“链接”，真正的图片文件是存在腾讯云 COS 里的。
>   - 如果你只删数据库记录，不删 COS 文件，那个图片会一直存在你的云存储里。
>   - **后果**：你的服务器里存了几千个“孤儿图片”，没人引用它们，但你每个月还得为它们付**存储费**。
> - 整洁（资源管理）：
>   - 避免云存储里堆积大量无用的垃圾文件，以后想找图都找不到。
>
> ### **🗑️ 为什么要删除资源2（对话历史）？**
>
> - 防止脏数据：
>   - 应用都删了，这个应用产生的聊天记录就没意义了。
>   - 如果不删，数据库的 `chat_history` 表里会留下一堆 `appId` 指向一个不存在的应用的记录。
> - 节省数据库空间：
>   - 聊天记录通常数据量很大，不清理会无谓地占用数据库磁盘。
>
> ### **📌 总结**
>
> `super.removeById(id)` 只是撕掉了**目录卡片**，而资源清理 1 和 2 是为了把**书架上的书**和**仓库里的货**也一起扔掉。这就是所谓的**“数据一致性”**和**“资源闭环”**。
