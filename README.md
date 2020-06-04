# TextVoiceHelper
Automatically broadcast We-Chat text messages plugin.

# 针对6.5.4版本制作的语音播报插件（此项目仅供逆向学习使用，若商用后果自负，作者不承担任何法律责任）

###  此篇文章是近期做的一款语音插件的总结，如有不周之处，望各位看官批评指出。
*  针对Android逆向项目，目前使用相对广的技术就属Xposed框架了，但是目前Xposed有个不足指出就是必须要求收集Root，当然也有不需Root方案，后续时间若有完美的解决方案会再相对记录（也有朋友指出使用AccessibilityService也可以达到类似效果，感兴趣的小伙伴可以试下）。
    * Xposed在语法上来说是JAVA的反射机制，核心模块各位感兴趣的逆友可以研究下，此文只简单介绍Xposed使用以及此文本Demo遇到的一些问题，以及解决方案。
    * 对于一些概念性的文章、下载模块请参考如下官方渠道
    * https://forum.xda-developers.com/showthread.php?t=3034811
    * 我起初理念是逆向是可以辅助正常APP开发的，单慢慢发现她也有自己的独到之处，她涉及到的东西可能相对纯APP开发有些杂乱，但我相信，经过慢慢的梳理，还是有完整且清晰方向的。
*   针对初次集成Xposed框架的朋友们来说，在gradle中配置更为轻便简单，如图：
 
    ![](https://user-gold-cdn.xitu.io/2019/7/4/16bbaee1b653e0ec?w=549&h=60&f=png&s=8973)
*   需要手机先装Xposed插件，针对不同机型，有不同模块的安装，到时候各位针对自身机型来安装即可    
    * 项目插件中需要在assets资源目录下创建命名为xposed_init的文件，里面声明好自身插件入口启动类，如图：
![](https://user-gold-cdn.xitu.io/2019/7/4/16bbaf117778047e?w=454&h=55&f=png&s=2849)   
    * 当然，声明的初始类可有多个，具体看自身使用场景需求。
* 既然要做基于别的APP文本播报的语音插件，那么你就要即时获取目标APP的即时数据内容，逆向项目最耗时最费力的模块莫过于定位目标APP的代码，因为不像正常APP开发，有明确技术耗时与定期，定位代码并Hook无误执行是有一定盲区的，很难精确定位的。
    * 若解决WC文本信息获取的方案，目前认为有两种：1，通过目标APP的数据库用SQL语句进行数据的即时查找。2， 完整HOOK住WC的通讯内置接口与API（不过这块肯定是相对耗时的，稳定性也不确定）。所以，此文本播报项目我采用是第一种方案，速战速决，毕竟时间成本太高做什么东西也相对意义上就会大大折扣，项目依然、生活依然。
    * 至于WC数据库解密方案，各位Google自行解决，此文不做介绍。
*  现在进入代码环节,首先在Xposed项目初始化时，Hook住目标APP的包名，就可以和其进程绑定，做对应Hook处理。
    * CallingTheDog为本插件的入口类，用来初始化检测WC的主进程，以及WC的APP主UI（LauncherUI）的启动监听、数据库Cursor游标对象的获取。
    *       public class CallingTheDog implements IXposedHookLoadPackage {

             //Specify the currently required version.
             public static String currentVersion = WE_CHAT_FLAG.VERSION_6_5_4;
             private WeChatLauncherUI weChatLauncherUI;
             private WeChatDBHelper weChatDBHelper;

             @Override
            public void handleLoadPackage(XC_LoadPackage.LoadPackageParam LPParam) throws Throwable {

             if (APP_PACKAGE_NAME.WE_CHAT.equals(LPParam.packageName)) {
                 if (weChatLauncherUI == null) {
                      LoggerUtils.xd("We Chat init.");
                      weChatLauncherUI = new WeChatLauncherUI(LPParam);
                 }

                 toHookWeChatAttach(LPParam);
             }
          }

             private void toHookWeChatAttach(final XC_LoadPackage.LoadPackageParam lpParam) {
             findAndHookMethod(Application.class, "attach", Context.class,
                        new XC_MethodHook() {
                           @Override
                          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                             super.afterHookedMethod(param);
                             if (weChatDBHelper == null) {
                                 LoggerUtils.xd("WeChatDBHelper init.");
                                 weChatDBHelper = new WeChatDBHelper();
                                 weChatDBHelper.init(lpParam);
                              }
                         }
                      });
                 }
             }
   
  * WC的Cursor数据库游标对象获取代码如下
  
    *       public class WeChatDBHelper {

             public static Method method = null;
             public static Object receiver = null;

             /**
             * 微信Cursor读写初始化。
             */
             public void init(final XC_LoadPackage.LoadPackageParam    loadPackageParam){
                 String targetSqlClass = "";
                if(WE_CHAT_FLAG.VERSION_6_5_4.equals(CallingTheDog.currentVersion)){
                    targetSqlClass = "com.tencent.mm.bg.g";
             }else if(WE_CHAT_FLAG.VERSION_7_0_4.equals(CallingTheDog.currentVersion)){
                 targetSqlClass = "com.tencent.mm.bb.g";
              }
              String targetSqlMethod = "rawQuery";
             findAndHookMethod(targetSqlClass, loadPackageParam.classLoader, targetSqlMethod,
                 String.class, String[].class, new XC_MethodHook() {
                       @Override
                       protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            //hook数据库连接对象，用于发起数据主动查询
                           if(method == null){
                              method = (Method) param.method;
                              receiver = param.thisObject;
                         }
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        //监听所有的消息查询语句
                        Cursor result = (Cursor) param.getResult();
                        String sqlStr = String.valueOf(param.args[0]);
                        if (result != null && result.getCount()>0 && sqlStr.startsWith("select * from message")) {
                            MsgListeners.listenerPath(result, loadPackageParam);
                        }
                    }
                });
            }
            }
*   借用游标对象获取文本信息的监听管理类，根据监听WC数据库插入的新消息数据来进行即时播报处理
    *     public class MsgListeners {

            private static Socket mClientSocket;
            private static PrintWriter mClientPrintWriter;

            public static void listenerPath(Cursor cursor, XC_LoadPackage.LoadPackageParam loadPackageParam) {
            //主动发送的status=2,接收的为3
              int status = WeChatMessage.getStatus(cursor);
              WeChatMessage.Type msgType = WeChatMessage.getType(cursor);

              //建立Client端
              if (mClientPrintWriter == null) {
                    new Thread() {
                       @Override
                       public void run() {
                        connectTCPServer();
                    }
                }.start();
             } else {
              LoggerUtils.xd("mClientPrintWriter is " + mClientPrintWriter);
            }
             switch (msgType) {
              case TEXT_MESSAGE:
                 List<TextMessage> textMessages = WeChatMessage.getTextMessage(cursor);

                    //初始化监测音量键提供者。
                  VolumeProvider volumeProvider = new VolumeProvider(loadPackageParam);

                    for (TextMessage textMessage : textMessages) {
                     if (textMessage == null) {
                        continue;
                    }
                    if (textMessage.getCreateTime() > WeChatMessage.lastSend) {
                        WeChatMessage.lastSend = textMessage.getCreateTime();
                        //处理转发的消息
                    }

                    SystemClock.sleep(1000);
                    String textContent = QueryWeChatDB.getNickname(textMessage.getFromUser())
                            + " 来消息:  " + textMessage.getContent();
                    LoggerUtils.xd("GenialSir Msg textContent " + textContent);

                    if (!TextUtils.isEmpty(textContent) && mClientPrintWriter != null) {
                        mClientPrintWriter.println(textContent);
                        LoggerUtils.xd("GenialSir mClientPrintWriter textContent " + textContent);
                    } else {
                        LoggerUtils.xd("mClientPrintWriter is null.. ");
                        new Thread() {
                            @Override
                            public void run() {
                                connectTCPServer();
                            }
                        }.start();
                    }
                }
                break;
            case VIDEO_MESSAGE:
                break;
            case IMG_MESSAGE:
                break;
            default:
                break;
            }
            }
          }
* 此处进行核心的语音播报模块，如果是Android系统>=21，则直接使用原生API的TextToSpeech即可实现语音播报，若Android系统<21，则TextToSpeech不支持中文。
    * 当然，此处都是有解决方案的，我们首先可以想到使用三方语音API，如讯飞、百度语音等都可以实现，那么在初次使用过程中必然会有写问题的。比如语音API的初始化问题，APP的key签名注册问题。显然，这块我们直接使用WC的Context注册是有大问题的，具体细节感兴趣的同学可以阅读他们底层源码。
    * 那么怎么解决呢？可以试着使用自身插件的Context进行一个三方语音注册，这款项目我使用的是讯飞语音，当然其它语音也可以。既然要使用自身插件Context，那么又会遇到一个问题！进程间通讯的问题，这个又怎么解决呢？
    * 起初想直接使用ALDL的机制，发现有数据类型的限制，AIDL并不是所有的数据类型都是可以使用的，可供支持的数据类型如下
        * 基本数据类型（int、long、char、boolean、double等）；
        * String和CharSequence；
        * List：只支持ArrayList，里面每个元素都必须被AIDL支持；
        * Map：只支持HashMap，里面每个元素都必须被AIDL支持，包括key和value；
        * Parcelable：所有实现了Parceable接口的对象；
        * AIDL：所有的AIDL接口本身也可以在AIDL文件中使用（AIDL接口中只支持方法，不支持声明静态常量，这一点区别于传统接口）。
    * 这块需要将插件的Context传递给微信进程中来进行初始化讯飞语音API的操作，整体感觉这种通讯不太清晰，有点过于麻烦，并且要兼容Context也是多了些不必要的手段。那么还有木有能稍微清晰的流程，简便的方案呢？
        * 肯定还是有的，那么我们可以借助Socket来进行网络数据的传输。
        * 假设我们将WC文本数据监听这块视为Socket的客户端，自身插件注册服务视为Socket服务端，那么整件语音播报处理流程是不是更清晰简洁了呢？
* Socket客户端代码
    *       private static void connectTCPServer() {
    
             Socket socket = null;
             while (socket == null) {
                try {
                  socket = new Socket("localhost", 8688);
                  mClientSocket = socket;
                  mClientPrintWriter = new PrintWriter(new BufferedWriter(
                         new OutputStreamWriter(socket.getOutputStream())), true);

                 LoggerUtils.xd("genial sir connect tcp server success.");
              } catch (IOException e) {
                  e.printStackTrace();
                  SystemClock.sleep(1000);
                  LoggerUtils.xd("genial sir connect tcp server failed, retry...");
             }
             }

            try {
             //接受服务器端的消息
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             while (true) {
                 LoggerUtils.xd("genial sir receive br : " + br);
                 String msg = br.readLine();
                 LoggerUtils.xd("genial sir receive : " + msg);
                 if (msg.contains("Close Socket service")) {
                        break;
                    }
             }
             LoggerUtils.xd("genial sir quit...");
             CloseUtils.closeQuietly(mClientPrintWriter);
             CloseUtils.closeQuietly(br);
             socket.close();
          } catch (IOException e) {
             e.printStackTrace();
             }
             }

*   Socket服务端初始化代码与实现代码
    *       private void initSocket() {
            //启动Socket服务类
            Intent serviceIntent = new Intent(MainActivity.this, VoiceSocketManager.class);
            startService(serviceIntent);
            }

    *       public class VoiceSocketManager extends Service {

            private TTSUtils ttsUtils;
            private boolean mIsServiceDestroyed = false;

            @Override
            public void onCreate() {
                super.onCreate();
                new Thread(new TcpServer()).start();
            }

            @Nullable
            @Override
            public IBinder onBind(Intent intent) {
                return null;
            }


            private class TcpServer implements Runnable {

                @Override
                public void run() {
                    ServerSocket serverSocket;
                    try {
                        //监听本地8688端口
                     serverSocket = new ServerSocket(8688);
                    } catch (IOException e) {
                        LoggerUtils.d("establish tcp server failed, port:8688");
                        e.printStackTrace();
                        return;
                    }
                    while (!mIsServiceDestroyed) {
                        try {
                            //接受客户端请求
                            final Socket client = serverSocket.accept();
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        responseClient(client);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        } catch (Exception e) {
                            LoggerUtils.d("error " + e.toString());
                            e.printStackTrace();
                        }
                    }
                }
             }

            private void responseClient(Socket client) throws IOException {
                Context applicationContext =         getApplication().getApplicationContext();
                initXF(applicationContext);
                //用于接受客户端消息
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                //用于向客户端发送消息
                PrintWriter printWriter = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(client.getOutputStream())), true);


                String clientContent;
                while (true) {
                    clientContent = in.readLine();
                    LoggerUtils.d("responseClient msg from client: " + clientContent);
                    if ("Close socket".equals(clientContent)) {
                        //客户端断开链接
                        if (ttsUtils != null) {
                            ttsUtils.speak("我是Voice Socket Manager, 客户端请求断开链接，撒哟啦啦");
                }
                        LoggerUtils.d("客户端断开链接.");
                        break;
                    }
                    if (ttsUtils != null) {
                        ttsUtils.speak(clientContent);
                    }else {
                        LoggerUtils.e("ttsUtils is null.");
                    }
                }
                LoggerUtils.d("client quit.");
                //关闭流
                CloseUtils.closeQuietly(printWriter);
                CloseUtils.closeQuietly(in);
                client.close();
            }

            private void initXF(Context context) {
                SpeechUtility.createUtility(context, "appid=5d07631c");
                Setting.setShowLog(true);
                ttsUtils = TTSUtils.getInstance(context);
                ttsUtils.init();
            }

            @Override
            public void onDestroy() {
                mIsServiceDestroyed = true;
                super.onDestroy();
            }
            }
*   重点来了，项目Github地址，热烈欢迎来全球最大同性交友网站进行star，你的star会增添改进的动力。
    * https://github.com/GenialSir/TextVoiceHelper.git    
    * 转发注明出处即可，撒花。

![](https://user-gold-cdn.xitu.io/2019/7/4/16bbbf382d0b40a7?w=1104&h=972&f=jpeg&s=136859)
