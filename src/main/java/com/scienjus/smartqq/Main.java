package com.scienjus.smartqq;

import com.linshixun.util.Fortunetelling;
import com.linshixun.util.Serial;
import com.linshixun.util.Turing;
import com.scienjus.smartqq.callback.MessageCallback;
import com.scienjus.smartqq.client.SmartQQClient;
import com.scienjus.smartqq.model.*;
import com.sun.deploy.util.ArrayUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {


    static Long licenseDate;

    static HashSet<Long> groups = new HashSet<>();
    static HashSet<Long> friends = new HashSet<>();
    static HashSet<Long> discusses = new HashSet<>();

    private static List<Friend> friendList = new ArrayList<>();                 //好友列表
    private static List<Group> groupList = new ArrayList<>();                   //群列表
    private static List<Discuss> discussList = new ArrayList<>();               //讨论组列表
    private static Map<Long, Friend> friendFromID = new HashMap<>();            //好友id到好友映射
    private static Map<Long, Group> groupFromID = new HashMap<>();              //群id到群映射
    private static Map<Long, GroupInfo> groupInfoFromID = new HashMap<>();      //群id到群详情映射
    private static Map<Long, Discuss> discussFromID = new HashMap<>();          //讨论组id到讨论组映射
    private static Map<Long, DiscussInfo> discussInfoFromID = new HashMap<>();  //讨论组id到讨论组详情映射

    static HashMap<String, String> faq;

    static HashMap<Long, HashMap<String, String>> lastContent = new HashMap<>();
    static ArrayList<String> keyword = new ArrayList<>();
    static HashSet<String> snowflakes = new HashSet<>();
    static Object[] snowflakesarray;

    static {

        Collections.addAll(keyword, new String[]{"关闭服务", "打开服务", "测字"});


        String s = "✽❁❃❋❋❂✱✲✳✵✵✸✸✺✻✼❄❅❆❇✺✻✼❄❅❆❇❉❊❉❊";
        for (char c : s.toCharArray()) {
            snowflakes.add(String.valueOf(c));
        }
        snowflakesarray = snowflakes.toArray();
    }


    public static Integer getRandom(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }


    public static void main(String[] args) {

        Long license = (Long) Serial.loadHessian(new File("license"));

        if (license == null) {
            System.out.println("License file cannot be found , please contact QQ:1990886924");
            System.exit(0);
        } else if (license < System.currentTimeMillis()) {
            System.out.println("License has expired !");
            System.exit(0);
        }

        faq = (HashMap<String, String>) Serial.loadHessian(new File("faq"));
        if (faq == null) {
            faq = new HashMap<>();
        }

        //创建一个新对象时需要扫描二维码登录，并且传一个处理接收到消息的回调，如果你不需要接收消息，可以传null
        final SmartQQClient client = new SmartQQClient();


        UserInfo accountInfo = client.getAccountInfo();
        String nick = accountInfo.getNick();
        keyword.add(nick);


        client.setCallBack(new MessageCallback() {
            public long delay = 5000L;

            @Override
            public void onMessage(Message message) {
                System.out.println(message.getContent());
            }

            private boolean openCloseService(GroupMessage msg, StringBuilder sb) {
                boolean ischange =false;
                boolean contains = groups.contains(msg.getGroupId());
                if (msg.getContent().contains("关闭服务")) {
                    removeGroup(msg.getGroupId());
                    sb.append("已关闭\n");
                    ischange = (contains != groups.contains(msg.getGroupId()));
                } else if (msg.getContent().contains("打开服务")) {
                    addGroup(msg.getGroupId());
                    sb.append("已打开\n");
                    ischange = (contains != groups.contains(msg.getGroupId()));
                }

                return ischange;
            }

            @Override
            public void onGroupMessage(GroupMessage msg) {
                System.out.println(msg);
                //记录用户之前说了什么
                HashMap<String, String> lastMsg = lastContent.get(msg.getGroupId());
                if (lastMsg == null) {
                    lastMsg = new HashMap<>();
                    lastContent.put(msg.getGroupId(), lastMsg);
                }

                //返回当前说话用户nick
                String msgNick = getGroupUserNick(msg, client);


                StringBuilder sb = new StringBuilder();

                if (!msgNick.equals(nick)) {
                    //别人说

                    if (msg.getContent().contains("@" + nick)) {
                        //@我

                        //私有消息
                        boolean ischange = openCloseService(msg, sb);

                        if (groups.contains(msg.getGroupId())) {

                            if (!command(msg, lastMsg, msgNick, sb) && !ischange) {
                                try {
                                    String anser = Turing.getAnser(msg.getContent().replaceAll(nick, ""), msgNick);
                                    if (anser != null)
                                        sb.append(anser);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }


                        } else {
                            sb.append("我已关闭\n请说: @" + nick + " 打开服务");
                        }

                        if (sb.length() == 0) {
                            sb.append("我不知道说什么了,等我的主人回来教教我\n");
                            sb.append("学习的口令是:@" + nick + " 学xxx答yyy\n");
                        }


                    } else if (msg.getContent().contains("@")) {
                        //@他人
                        return;
                    } else {
                        //无@
                        //公共消息
                        if (groups.contains(msg.getGroupId())) {
                            if (msg.getContent().contains("手续费") || msg.getContent().contains("百分百") || msg.getContent().contains("加微信") || msg.getContent().contains("包下款") || msg.getContent().contains("无视黑白") || msg.getContent().contains("http://") || msg.getContent().contains("https://")) {
                                sb.append("管理员 ," + msgNick + "正在传播小广告\n");
                            } else if (msg.getContent().contains("红包")) {
                                sb.append("重大申明，本人拒收来自支付宝的任何形式的转账，要转账请直接转我qq钱包\n");
                            } else if (msg.getContent().contains("调戏")) {
                                sb.append("管理员 ," + msgNick + "正在调戏妇女儿童\n");
                            } else if (msg.getContent().contains("测字")) {
                                Pattern compile = Pattern.compile("[\\S\\s]*?测字([\\S\\s]{3,})");
                                Matcher matcher = compile.matcher(msg.getContent().trim());
                                if (matcher.find()) {
                                    try {
                                        String anser = Fortunetelling.getAnser(matcher.group(1).trim());

                                        if (anser != null) {
                                            sb.append(anser);
                                        } else {
                                            sb.append("测字需要三个汉字呢\n如: 测字 王重阳");
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    sb.append("测字需要三个汉字呢\n如: 测字 王重阳");
                                }

                            } else {

                                faq.forEach((a, q) -> {
                                    if (msg.getContent().contains(a)) {
                                        sb.append(q);
                                    }
                                });

                                if (sb.length() == 0) {
                                    try {
                                        String anser = Turing.getAnser(msg.getContent().replaceAll(nick, ""), msgNick);

                                        if (anser != null)
                                            sb.append(anser);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    if (sb.length() > 0) {
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        client.sendMessageToGroup(msg.getGroupId(), sb.append("@" + msgNick).toString());
                    }

                    lastMsg.put(msgNick, msg.getContent());
                } else {

                    //我说
                    if (snowflakes.contains(msg.getContent().substring(msg.getContent().length() - 1))) {
                        //我回复
                        return;
                    } else {
                        //我的指令
                        openCloseService(msg, sb);
                        command(msg, lastMsg, msgNick, sb);

                        if (sb.length() > 0) {
                            try {
                                Thread.sleep(delay);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            client.sendMessageToGroup(msg.getGroupId(), sb.append("@" + msgNick).toString());

                        }
                    }

                }
            }

            @Override
            public void onDiscussMessage(DiscussMessage message) {
                System.out.println(message.getContent());
            }
        });


        friendList = client.getFriendList();                //获取好友列表
        groupList = client.getGroupList();                  //获取群列表
        discussList = client.getDiscussList();              //获取讨论组列表
        for (Friend friend : friendList) {                  //建立好友id到好友映射
            friendFromID.put(friend.getUserId(), friend);
        }
        for (Group group : groupList) {                     //建立群id到群映射
            groupFromID.put(group.getId(), group);
        }
        for (Discuss discuss : discussList) {               //建立讨论组id到讨论组映射
            discussFromID.put(discuss.getId(), discuss);
        }
        //登录成功后便可以编写你自己的业务逻辑了
        Scanner scan = new Scanner(System.in);
        while (true) {

            try {


                System.out.println("功能菜单:" +
                        "\n1.查看所有群" +
                        "\n2.查看所有讨论组" +
                        "\n3.查看所有好友" +
                        "\n4.添加要管理的群" +
                        "\n5.删除要管理的群" +
                        "\n6.添加要管理的好友" +
                        "\n7.添加指定群的所有好友" +
                        "\n8.导出指定群的好友列表" +
                        "\n9.群发指定列表好友信息" +
                        "\n10.分析指定群的聊天主题" +
                        "\n11.导出指定群的聊天记录" +
                        "\n12.清空指定群的聊天记录");


                String read = scan.nextLine();

                switch (read) {
                    case "1":
                        List<Group> groupList = client.getGroupList();
                        for (Group group : groupList) {
                            System.out.println(group);
                        }
                        break;
                    case "2":
                        List<Discuss> discussList = client.getDiscussList();
                        for (Discuss discuss : discussList) {
                            System.out.println(discuss);
                        }
                        break;
                    case "3":
                        List<Category> categories = client.getFriendListWithCategory();
                        for (Category category : categories) {
                            System.out.println(category.getName());
                            for (Friend friend : category.getFriends()) {
                                System.out.println(friend);
                            }
                        }
                        break;
                    case "4":
                        Long aLong = Long.valueOf(scan.nextLine());
                        addGroup(aLong);
                        System.out.println("添加群成功" + aLong);
                        break;
                    case "5":
                        Long aLong2 = Long.valueOf(scan.nextLine());
                        removeGroup(aLong2);
                        System.out.println("添加群成功" + aLong2);
                        break;
                    case "6":
                        break;
                    case "7":
                        break;
                    case "8":
                        break;
                    case "9":
                        break;
                    default:
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {


            }

        }

    }

    public static void addGroup(Long groupId) {
        groups.add(groupId);
    }

    public static void removeGroup(Long groupId) {
        groups.remove(groupId);
    }

    private static boolean command(GroupMessage msg, HashMap<String, String> lastMsg, String msgNick, StringBuilder sb) {
        boolean flag = false;
        if (msg.getContent().contains("刚刚说什么")) {
            String learnRegx = "@([\\S\\s]+?)[\\s]*刚刚说什么";
            Matcher matcher = Pattern.compile(learnRegx).matcher(msg.getContent());
            if (matcher.find()) {
                if (lastMsg.containsKey(matcher.group(1))) {
                    sb.append(matcher.group(1) + "最后一条消息是:" + lastMsg.get(matcher.group(1)));
                } else {
                    sb.append("我也不知道呀");
                }
            }
            flag = true;
        } else if (msg.getContent().contains("删除")) {
            int size = faq.size();

            Pattern compile = Pattern.compile("删除([\\S\\s]+?)$");
            Matcher matcher = compile.matcher(msg.getContent().trim());
            if (matcher.find()) {
                faq.remove(matcher.group(1));
                sb.append("已经删除问题:" + matcher.group(1));
            }

            if (size != faq.size()) {
                Serial.storeHessian(faq, "faq");
            }
            flag = true;
        } else if (msg.getContent().contains("学") && msg.getContent().contains("答")) {
            int size = faq.size();
            String learnRegx = "[\\S\\s]*学[\\S\\s]+?答[\\S\\s]+";
            String content = msg.getContent();
            if (content.matches(learnRegx)) {
                Pattern compile = Pattern.compile("学([\\S\\s]+?)答([\\S\\s]+)");
                Matcher matcher = compile.matcher(msg.getContent().trim());
                if (matcher.find()) {


                    String group = matcher.group(1).trim();
                    String group1 = matcher.group(2).trim();

                    for (String s : keyword) {
                        group = group.replaceAll(s, "");
                        group1 = group1.replaceAll(s, "");
                    }
                    faq.put(group, group1 + "\n由[" + msgNick + "]提供");
                    sb.append("我又学习到新技能咯: 问: " + group + " 答: " + group1 + "\n删除的口令是:宝宝 删除xxx");
                }

            }

            if (size != faq.size()) {
                Serial.storeHessian(faq, "faq");
            }
            flag = true;
        } else if (msg.getContent().contains("测字")) {
            Pattern compile = Pattern.compile("[\\S\\s]*?测字([\\S\\s]{3,})");
            Matcher matcher = compile.matcher(msg.getContent().trim());
            if (matcher.find()) {
                try {
                    String anser = Fortunetelling.getAnser(matcher.group(1).trim());

                    if (anser != null) {
                        sb.append(anser);
                    } else {
                        sb.append("测字需要三个汉字呢\n如: 测字 王重阳");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                sb.append("测字需要三个汉字呢\n如: 测字 王重阳");
            }
            flag = true;
        }
        return flag;
    }

    public static Object getRandomSnow() {
        return snowflakesarray[getRandom(0, snowflakesarray.length - 1)];
    }


    /**
     * 获取群id对应群详情
     *
     * @param id 被查询的群id
     * @return 该群详情
     */
    public static GroupInfo getGroupInfoFromID(Long id, SmartQQClient client) {
        if (!groupInfoFromID.containsKey(id)) {
            groupInfoFromID.put(id, client.getGroupInfo(groupFromID.get(id).getCode()));
        }
        return groupInfoFromID.get(id);
    }

    /**
     * 获取讨论组id对应讨论组详情
     *
     * @param id 被查询的讨论组id
     * @return 该讨论组详情
     */
    public static DiscussInfo getDiscussInfoFromID(Long id, SmartQQClient client) {
        if (!discussInfoFromID.containsKey(id)) {
            discussInfoFromID.put(id, client.getDiscussInfo(discussFromID.get(id).getId()));
        }
        return discussInfoFromID.get(id);
    }

    /**
     * 获取群消息所在群名称
     *
     * @param msg 被查询的群消息
     * @return 该消息所在群名称
     */
    public static String getGroupName(GroupMessage msg) {
        return getGroup(msg).getName();
    }

    /**
     * 获取讨论组消息所在讨论组名称
     *
     * @param msg 被查询的讨论组消息
     * @return 该消息所在讨论组名称
     */
    public static String getDiscussName(DiscussMessage msg) {
        return getDiscuss(msg).getName();
    }

    /**
     * 获取群消息所在群
     *
     * @param msg 被查询的群消息
     * @return 该消息所在群
     */
    public static Group getGroup(GroupMessage msg) {
        return groupFromID.get(msg.getGroupId());
    }

    /**
     * 获取讨论组消息所在讨论组
     *
     * @param msg 被查询的讨论组消息
     * @return 该消息所在讨论组
     */
    public static Discuss getDiscuss(DiscussMessage msg) {
        return discussFromID.get(msg.getDiscussId());
    }

    /**
     * 获取私聊消息发送者昵称
     *
     * @param msg 被查询的私聊消息
     * @return 该消息发送者
     */
    public static String getFriendNick(Message msg) {
        Friend user = friendFromID.get(msg.getUserId());
        if (user.getMarkname() == null || user.getMarkname().equals("")) {
            return user.getNickname(); //若发送者无备注则返回其昵称
        } else {
            return user.getMarkname(); //否则返回其备注
        }

    }

    /**
     * 获取群消息发送者昵称
     *
     * @param msg 被查询的群消息
     * @return 该消息发送者昵称
     */
    public static String getGroupUserNick(GroupMessage msg, SmartQQClient client) {
        for (GroupUser user : getGroupInfoFromID(msg.getGroupId(), client).getUsers()) {
            if (user.getUin() == msg.getUserId()) {
                if (user.getCard() == null || user.getCard().equals("")) {
                    return user.getNick(); //若发送者无群名片则返回其昵称
                } else {
                    return user.getCard(); //否则返回其群名片
                }
            }
        }
        return "系统消息"; //若在群成员列表中查询不到，则为系统消息
        //TODO: 也有可能是新加群的用户或匿名用户
    }

    /**
     * 获取讨论组消息发送者昵称
     *
     * @param msg 被查询的讨论组消息
     * @return 该消息发送者昵称
     */
    public static String getDiscussUserNick(DiscussMessage msg, SmartQQClient client) {
        for (DiscussUser user : getDiscussInfoFromID(msg.getDiscussId(), client).getUsers()) {
            if (user.getUin() == msg.getUserId()) {
                return user.getNick(); //返回发送者昵称
            }
        }
        return "系统消息"; //若在讨论组成员列表中查询不到，则为系统消息
        //TODO: 也有可能是新加讨论组的用户
    }
}
