package com.jiang.simbot.listener.group;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jiang.simbot.utils.ReciteRequestUtil;
import kotlinx.coroutines.TimeoutCancellationException;
import love.forte.simboot.annotation.ContentTrim;
import love.forte.simboot.annotation.Filter;
import love.forte.simboot.annotation.FilterValue;
import love.forte.simboot.annotation.Listener;
import love.forte.simboot.filter.MatchType;
import love.forte.simbot.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @FileName :ReciteWords
 * @Description:背单词，帮助群友提升英语水平。
 * @Author :jdk
 * @date :2023-09-08
 **/
@Component
public class ReciteWords {
    private static final Logger logger = LoggerFactory.getLogger(ReciteWords.class);

    @Autowired
    @Qualifier("redisTemplate0")
    private StringRedisTemplate stringRedisTemplate0;//存开关
    @Autowired
    @Qualifier("redisTemplate1")
    private StringRedisTemplate stringRedisTemplate1;//存请求来的单词
    @Autowired
    @Qualifier("redisTemplate4")
    private StringRedisTemplate stringRedisTemplate4;//记录单词编号和对应会还是不会。例如：1：会
    @Autowired
    @Qualifier("redisTemplate5")
    private StringRedisTemplate stringRedisTemplate5;//用一个数记录某人背多少单词了
//    private String one;
    @Listener(priority = 1)
    @ContentTrim
    @Filter(value = "背单词", matchType = MatchType.REGEX_MATCHES)
    public EventResult reciteNewWord(FriendMessageEvent event) throws IOException {
        stringRedisTemplate0.opsForValue().set(event.getFriend().getId().toString(), "reciting");
        //清空之前的单词库
        stringRedisTemplate1.delete(event.getFriend().getId().toString());
        Set<String> words = getWords(event, "new", String.valueOf(0));
        String element = null;
        Iterator<String> iterator = words.iterator();
        if (iterator.hasNext()) {
            element = iterator.next();
            JSONObject jsonObject = JSONUtil.parseObj(element);
            String word = jsonObject.getStr("word");
            String no = jsonObject.getStr("no");
            stringRedisTemplate5.opsForValue().set(event.getFriend().getId().toString(),no);
            stringRedisTemplate4.opsForHash().put(event.getFriend().getId().toString(),stringRedisTemplate5.opsForValue().get(event.getFriend().getId().toString()),"待定");
            event.getFriend().sendBlocking(word);
        }
        return EventResult.truncate();
    }
    @Listener(priority = 1)
    @ContentTrim
    @Filter(value = "结束",matchType = MatchType.REGEX_MATCHES)
    public EventResult stopReciteWord(FriendMessageEvent event){
        String AuthorId = event.getFriend().getId().toString();
        stringRedisTemplate0.opsForValue().set(AuthorId,"recited");
        String s = stringRedisTemplate0.opsForValue().get(AuthorId);
        if (s.equals("recited")){
            event.getFriend().sendBlocking("已经结束");
            logger.info(event.getFriend().getUsername()+"已经结束了背单词");
        }else {
            event.getFriend().sendBlocking("结束背单词遇到错误");
        }
        //把这次请求的单词清掉
        Boolean delete = stringRedisTemplate1.delete(AuthorId);
        logger.info(event.getFriend().getUsername()+""+"删除成功"+delete);
        return EventResult.truncate();
    }

    @Listener(priority = 2)
    @ContentTrim
    public EventResult listenReciteWords(FriendMessageEvent event) throws IOException {
        String AuthorId = event.getFriend().getId().toString();
        String value = stringRedisTemplate0.opsForValue().get(AuthorId);
        String message = event.getMessageContent().getPlainText().trim();
        if (value != null && value.equals("reciting")) {
            if (!(message.equals("认识")||message.equals("不认识"))){
                event.getFriend().sendBlocking("格式不正确(认识/不认识)");
               return EventResult.truncate();
            }
            //修改value为"认识或不认识"
            String AuthorNo = stringRedisTemplate5.opsForValue().get(AuthorId);
            stringRedisTemplate4.opsForHash().put(AuthorId,AuthorNo,message);
            //将认识或者不认识传回去
            String status = null;
            if (message.equals("认识")){
                status="1";
            } else if (message.equals("不认识")){
                status="0";
            }

            String s = ReciteRequestUtil.sendStatus(AuthorId, AuthorNo, status);
            logger.info(s);
            //现在可以发意思了
            double parseDouble = Double.parseDouble(AuthorNo);
            Set<String> membersWithSpecificScore = stringRedisTemplate1.opsForZSet().rangeByScore(AuthorId,parseDouble,parseDouble);
            Iterator<String> iterator1 = membersWithSpecificScore.iterator();
            if (iterator1.hasNext()) {
                String content = iterator1.next();
                JSONObject jsonObject1 = JSONUtil.parseObj(content);
                String word = jsonObject1.getStr("content");
                event.getFriend().sendBlocking(word);
                //然后删除这个数据
                stringRedisTemplate1.opsForZSet().remove(AuthorId,content);
            } else {
                System.out.println("迭代器中没有更多元素了");
            }

            //然后将4redis中存的数据清空，以保持始终是一个
            stringRedisTemplate4.delete(AuthorId);
            //开始发下一个单词
            Set<String> member1 = stringRedisTemplate1.opsForZSet().range(AuthorId,0,0);
            String no1 = null;
            Iterator<String> iterator = member1.iterator();
            if (iterator.hasNext()) {
                String element = iterator.next();
                JSONObject jsonObject2 = JSONUtil.parseObj(element);
                String word1 = jsonObject2.getStr("word");
                String no = jsonObject2.getStr("no");
                no1=no;
                event.getBot().delay(Duration.ofSeconds(5),()->{
                    event.getFriend().sendBlocking(word1);
                });
            }
            System.out.println("no1 = " + no1);
            //完成本次背单词
            if (no1==null){
                event.getFriend().sendBlocking("你已经完成了这次任务，如果想继续背单词，可以输入👇面指令:新单词");
                return EventResult.truncate();
            }
            //将这个单词序号赋给redis5
            stringRedisTemplate5.opsForValue().set(AuthorId,no1);
            //更新redis4中的待定
            stringRedisTemplate4.opsForHash().put(AuthorId, stringRedisTemplate5.opsForValue().get(AuthorId),"待定");

        }
        return EventResult.truncate();
    }

    @Listener(priority = 1)
    @ContentTrim
    @Filter(value = "复习单词", matchType = MatchType.REGEX_MATCHES)
    public EventResult reviewWord(FriendMessageEvent event) throws IOException {
        stringRedisTemplate0.opsForValue().set(event.getFriend().getId().toString(), "reciting");
        //清空之前的单词库
        stringRedisTemplate1.delete(event.getFriend().getId().toString());
        Set<String> words = getWords(event, "record", String.valueOf(0));
        String element = null;
        Iterator<String> iterator = words.iterator();
        if (iterator.hasNext()) {
            element = iterator.next();
            JSONObject jsonObject = JSONUtil.parseObj(element);
            String word = jsonObject.getStr("word");
            String no = jsonObject.getStr("no");
            stringRedisTemplate5.opsForValue().set(event.getFriend().getId().toString(),no);
            stringRedisTemplate4.opsForHash().put(event.getFriend().getId().toString(),stringRedisTemplate5.opsForValue().get(event.getFriend().getId().toString()),"待定");
            event.getFriend().sendBlocking(word);
        }
        return EventResult.truncate();
    }

    @Listener(priority = 1)
    @ContentTrim
    @Filter(value = "注册", matchType = MatchType.REGEX_MATCHES)
    @Filter(value = "设置单词数", matchType = MatchType.REGEX_MATCHES)
    public EventResult regist(FriendMessageEvent event, ContinuousSessionContext sessionContext) throws IOException {

/*        JSONObject jsonObject = JSONUtil.parseObj(userRegist);
        String status = jsonObject.getStr("status");
        if (status.equals("-1")){
            event.getFriend().sendBlocking("设置失败");
        }else if(status.equals("1")){
            event.getFriend().sendBlocking("设置成功");*/
        String AuthorId = event.getFriend().getId().toString();
        event.getFriend().sendBlocking("请输入你首次想要背的单词的个数(纯数字)");
        try {
            sessionContext.waitingForNextMessage(AuthorId, FriendMessageEvent.Key, 59, TimeUnit.SECONDS, (e, c) -> {
                String trim = c.getMessageContent().getPlainText().trim();
                int parseInt = 0;
                try {
                    parseInt = Integer.parseInt(trim);
                } catch (Exception e1) {
                    event.getFriend().sendBlocking("数据不合法");
                    return true;
                }
                try {
                    String s = ReciteRequestUtil.userRegist(AuthorId, event.getFriend().getUsername(), parseInt);
                    event.getFriend().sendBlocking("注册成功");
                } catch (IOException ex) {
                    event.getFriend().sendBlocking("请求超时");
                    return true;
                }
                return true;
            });
        } catch (TimeoutCancellationException e) {
            event.getFriend().sendBlocking("等待超时，注册失败");
        }
        return EventResult.truncate();
    }

    @Listener(priority = 1)
    @ContentTrim
    @Filter(value = "获取信息", matchType = MatchType.REGEX_MATCHES)
    public EventResult getUserRecord(FriendMessageEvent event) throws IOException {
        String userRecordS = ReciteRequestUtil.getUserRecordS(event.getFriend().getId().toString());
        JSONObject jsonObject = JSONUtil.parseObj(userRecordS);
        String status = jsonObject.getStr("status");
        String msg = jsonObject.getStr("msg");
        System.out.println(msg);
        if (status.equals("-1")) {
            event.getFriend().sendBlocking(msg);
            return EventResult.truncate();
        }
        Integer i = jsonObject.getInt("bool");
        System.out.println(i);
        if (i == 1) {
            String data = jsonObject.getStr("data");
            JSONObject jsonObjectData = JSONUtil.parseObj(data);
            String name = jsonObjectData.getStr("name");
            String num = jsonObjectData.getStr("num");
            String use_times = jsonObjectData.getStr("use_times");
            String word_times = jsonObjectData.getStr("word_times");
            String max_achievement = jsonObjectData.getStr("max_achievement");
            String latest_achievement = jsonObjectData.getStr("latest_achievement");
            String update_time = jsonObjectData.getStr("update_time");
            String register_time = jsonObjectData.getStr("register_time");
            String record_word_num = jsonObjectData.getStr("record_word_num");
            String message = "----------" + name + "--------\n" +
                    "您当前设置一次背单词数量是: " + num + "\n" +
                    "您使用背单词功能共" + use_times + "\n" +
                    "您已使用该功能记了" + word_times + "\n" +
                    "您" + max_achievement + "\n" +
                    "您" + latest_achievement + "\n" +
                    "您上次使用时间是" + update_time + "\n" +
                    "您注册的时间是" + register_time;
            event.getFriend().sendBlocking(message);
          /*  String format = """
                    ----------" + name + "--------\\n"+
                    您共记了%s
                    您当前设置一次背单词数量是:%s
                    您使用背单词功能共%s
                    您已使用该功能记了%s
                    您%s
                    您%s
                    您上次使用时间是%d
                    您注册的时间是%d
                    """;
            String message = String.format(format,
                    record_word_num,
                    num,
                    use_times,
                    word_times,
                    max_achievement,
                    latest_achievement,
                    update_time,
                    register_time);
            event.getFriend().sendBlocking(message);
        } else if (i == 0) {
            event.getFriend().sendBlocking("用户未注册");
        }*/
        }
        return EventResult.truncate();
    }
    @Listener(priority = 1)
    @ContentTrim
    @Filter(value = "录入{{str}}",matchType = MatchType.REGEX_MATCHES)
    public EventResult recordWord(FriendMessageEvent event, @FilterValue("str") String str) throws IOException {
        System.out.println(str);
        if (str!=null){
            String recordWord = ReciteRequestUtil.recordWord(event.getFriend().getId().toString(), str, "record");
            System.out.println(recordWord);
            JSONObject jsonObject = JSONUtil.parseObj(recordWord);
            String msg = jsonObject.getStr("msg");

            event.getFriend().sendBlocking(msg);
        }
        return EventResult.truncate();
    }

    //获取单词
    public Set<String> getWords(FriendMessageEvent event, String type, String num) throws IOException {
        String words = ReciteRequestUtil.getWords(event.getFriend().getId().toString(), type, num);
        JSONObject jsonObject = JSONUtil.parseObj(words);
        Integer status = jsonObject.getInt("status");
        String msg = jsonObject.getStr("msg");
        Set<String> members = new HashSet<>();
        if (status == -1) {
            event.getFriend().sendBlocking(msg);
        } else if (status == 1) {
            event.getFriend().sendBlocking(msg);
            String data = jsonObject.getStr("data");
            JSONArray dataArray = JSONUtil.parseArray(data);
            //存到redis
            for (Object json : dataArray) {
                String jsonString = json.toString();
                JSONObject jsonObjectWord = JSONUtil.parseObj(jsonString);
                String no = jsonObjectWord.getStr("no");
                stringRedisTemplate1.opsForZSet().add(event.getFriend().getId().toString(), jsonString, (double) Integer.parseInt(no));
            }
            //只拿到score最小的单词
            Set<String> strings = stringRedisTemplate1.opsForZSet().range(event.getFriend().getId().toString(), 0, 0);
            members = strings;
        }
        return members;
    }

}
