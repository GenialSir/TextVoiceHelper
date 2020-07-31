# TextVoiceHelper
Automatically broadcast We-Chat text messages plugin.

#   微信插件之语音播报（此项目仅供逆向学习使用）
### 本意是为盲人群体做的一款微信语音辅助的小项目，虽然最终没有被启用，但其中涉及到的反编译思想（逆向思维）以及进程间通讯的模块还是对后续项目开发有一定裨益的。

*   对于Android逆向项目，本项目目前使用的技术是Xposed框架，但是Xposed目前所知是需要手机Root后才可使用。
*  Xposed设计思想是借用JAVA的反射机制来实现的，Hook所需模块来进行修改，从而达到自身需求。像微信机器人、滴滴出行之类的自动抢单等的插件。 
    * Xposted相关资料的网址
    	* https://forum.xda-developers.com/showthread.php?t=3034811

*	在AndroidStudio的gradle中配置如图：
 
    ![](https://user-gold-cdn.xitu.io/2019/7/4/16bbaee1b653e0ec?w=549&h=60&f=png&s=8973)
*   需要手机先装Xposed插件，针对不同机型，有对应模块的安装    
* 项目插件中需要在assets资源目录下创建命名为xposed_init的文件，里面声明好自身插件入口启动类，如图：
![](https://user-gold-cdn.xitu.io/2019/7/4/16bbaf117778047e?w=454&h=55&f=png&s=2849)   
    
* 既然要做基于微信消息文本播报的语音插件，那么就要即时获取目标APP的即时数据内容，逆向项目最耗时最费力的模块是定位所需的代码模块加调试，因此不像正常需求开发，会有明确技术耗时与定期。定位代码并Hook无误执行一般来说是有些难定位且很耗时的。
    * 若解决微信的聊天文本信息获取的方案，目前可行有两种：
    	*	1，通过目标APP的数据库用SQL语句进行数据的即时查找
        *	2，通过Hook微信在通讯时的调用消息的API（不过这块肯定是相对耗时的。所以，此文本播报项目我采用是第一种方案，毕竟时间成本太高的话，导致做出东西也相对意义上大打折扣。）
    * 对于微信的数据库解密方案，本文不做介绍。

###	思路与实现
*  首先在Xposed项目初始化时，实时检测微信进程，从而Hook住微信并在其运行时做对应的Hook处理。
    * CallingTheDog为本插件的入口类，用来初始化检测微信的主进程，以及微信的APP主UI（LauncherUI）的启动监听、数据库Cursor游标对象的获取。
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
   
  * 微信的Cursor数据库游标对象获取代码如下
  
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
*   借用游标对象获取文本信息的监听管理类，根据监听数据库插入的新消息数据来进行即时播报处理
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
* 再进行语音播报模块， ** 注意，如果是Android系统>=21（5.0），则直接使用原生API的TextToSpeech即可实现语音播报，若Android系统<21（5.0），则TextToSpeech不支持中文。 **
    * 如果需要支持中文，那首先可以想到使用三方语音API，如讯飞、百度语音等都可以实现，我在初次使用过程中遇到一些意料之外的问题：
     	*	语音API的初始化问题，APP的key签名注册问题。显然，这块直接使用微信的Context注册是有问题的。
    	*	如果不依赖微信的Context，可以使用自身插件的Context进行一个三方语音注册，我的 **TextVoiceHelper** 使用的是讯飞语音。但在使用自身插件的Context时，后面又遇到因进程间通讯而导致语音无法播报的问题。
    	* 在使用ALDL过程中，直接支持的数据类型如下：（本项目采用的是Socket）
        	* 基本数据类型（int、long、char、boolean、double等）；
        	* String和CharSequence；
       		* List：只支持ArrayList，里面每个元素都必须被AIDL支持；
        	* Map：只支持HashMap，里面每个元素都必须被AIDL支持，包括key和value；
        	* Parcelable：所有实现了Parceable接口的对象；
        	* AIDL：所有的AIDL接口本身也可以在AIDL文件中使用（AIDL接口中只支持方法，不支持声明静态常量，这一点区别于传统接口）。
    * 若采用将插件的Context传递给微信进程中来进行初始化讯飞语音API的操作，这个思路整体感觉很矛盾且不清晰，并且Context在多进程情况下也是问题很多。
        * 那么就不用在跨进程传输Context上下功夫，而是直接将聊天数据跨进程传输，从而借助Socket来进行跨进程传输通信，只需在**TextVoiceHelper**进程内注册的讯飞直接播报即可。也避免了使用微信Context初始化自己注册讯飞的尴尬与**TextVoiceHelper**在注册讯飞后，获取微信聊天数据遇到进程通讯的问题。
        * **按照上述思路，将微信文本数据监听这块视为Socket的发送端，自身插件注册服务视为Socket接受端，那么整体语音播报处理流程是不是更清晰简洁了呢？**
* 通过将微信与插件分为客户发送端与服务接受端，得到如下的Socket客户端代码：
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

*   Socket服务端初始化与实现：
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
*   项目**TextVoiceHelper**Github地址
    * https://github.com/GenialSir/TextVoiceHelper.git    
    * 转发注明出处即可，希望对正在阅读你有所启发与帮助



https://juejin.im/post/6855571707592343559/
