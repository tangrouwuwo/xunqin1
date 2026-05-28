package com.xunqin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xunqin.entity.AIChatMessage;
import com.xunqin.entity.MissingPerson;
import com.xunqin.mapper.AIChatMessageMapper;
import com.xunqin.service.AIService;
import com.xunqin.service.MissingPersonService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class AIServiceImpl implements AIService {

    private final MissingPersonService missingPersonService;
    private final AIChatMessageMapper aiChatMessageMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.deepseek.api.key}")
    private String deepseekApiKey;

    @Value("${ai.deepseek.api.url}")
    private String deepseekApiUrl;

    public AIServiceImpl(MissingPersonService missingPersonService, AIChatMessageMapper aiChatMessageMapper, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.missingPersonService = missingPersonService;
        this.aiChatMessageMapper = aiChatMessageMapper;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Map<String, Object>> searchByRegion(String region) {
        Page<MissingPerson> page = missingPersonService.searchMissingPersons(null, null, region, null, null, null, 1, 100);
        List<MissingPerson> missingPersons = page.getRecords();

        List<Map<String, Object>> result = new ArrayList<>();
        for (MissingPerson person : missingPersons) {
            Map<String, Object> personMap = new HashMap<>();
            personMap.put("id", person.getId());
            personMap.put("name", person.getName());
            personMap.put("gender", person.getGender());
            personMap.put("age", person.getCurrentAge());
            personMap.put("missingLocation", person.getMissingLocation());
            personMap.put("missingDate", person.getMissingDate());
            personMap.put("description", person.getDescription());
            Map<String, Object> contactInfo = new HashMap<>();
            contactInfo.put("name", person.getContactName());
            contactInfo.put("phone", person.getContactPhone());
            contactInfo.put("email", person.getContactEmail());
            personMap.put("contactInfo", contactInfo);
            personMap.put("status", person.getStatus());
            result.add(personMap);
        }
        return result;
    }

    @Override
    public String askQuestion(Long userId, String sessionId, String question) {
        saveChatMessage(userId, sessionId, "user", question);

        String systemAnswer = getSystemAnswer(question);
        if (systemAnswer != null) {
            saveChatMessage(userId, sessionId, "assistant", systemAnswer);
            return systemAnswer;
        }

        String aiAnswer = getAIAnswer(question);
        saveChatMessage(userId, sessionId, "assistant", aiAnswer);
        return aiAnswer;
    }

    private void saveChatMessage(Long userId, String sessionId, String role, String content) {
        AIChatMessage chatMessage = new AIChatMessage();
        chatMessage.setUserId(userId);
        chatMessage.setSessionId(sessionId);
        chatMessage.setRole(role);
        chatMessage.setContent(content);
        aiChatMessageMapper.insert(chatMessage);
    }

    private String getSystemAnswer(String question) {
        // 手机号/账号注册限制问题 - 最高优先级
        if ((question.contains("手机") || question.contains("电话") || question.contains("账号") || question.contains("注册")) && 
            (question.contains("两个") || question.contains("多个") || question.contains("重复") || question.contains("几次") || question.contains("两"))) {
            return "关于账号注册限制：\n1. 一个手机号只能注册一个账号\n2. 系统会对手机号进行唯一性校验，重复注册会提示'该手机号已被注册'\n3. 如果您忘记了之前注册的账号，可以使用'找回密码'功能\n4. 如需帮助，请联系网站管理员协助处理账号问题\n\n这样设计是为了保证账号安全和信息真实性。";
        }

        // 地区寻亲查询
        if (question.contains("寻亲") || question.contains("失踪") || question.contains("找人")) {
            if (question.contains("北京")) return getRegionAnswer("北京");
            else if (question.contains("上海")) return getRegionAnswer("上海");
            else if (question.contains("广州")) return getRegionAnswer("广州");
            else if (question.contains("深圳")) return getRegionAnswer("深圳");
            else if (question.contains("成都")) return getRegionAnswer("成都");
            else if (question.contains("杭州")) return getRegionAnswer("杭州");
            else if (question.contains("武汉")) return getRegionAnswer("武汉");
            else if (question.contains("重庆")) return getRegionAnswer("重庆");
            else if (question.contains("南京")) return getRegionAnswer("南京");
            else if (question.contains("西安")) return getRegionAnswer("西安");
        }

        // 志愿者注册流程
        if (question.contains("志愿者") && (question.contains("条件") || question.contains("加入") || question.contains("成为") || question.contains("申请"))) {
            return "成为志愿者的条件和流程：\n1. 在网站上注册账号\n2. 在个人中心找到'志愿者申请'入口\n3. 填写申请表，包括您的个人介绍、志愿服务的意愿和可投入的时间\n4. 提交申请后等待管理员审核\n5. 审核通过后您将成为正式志愿者\n\n志愿者可以参与的任务包括：协助搜索、线索核实、信息整理、协助发布寻亲信息等。";
        }

        // 如何发布寻亲信息
        if (question.contains("如何") && question.contains("发布")) {
            return "发布寻亲信息的步骤如下：\n1. 登录网站后，在导航栏中找到'发布寻亲信息'按钮并点击\n2. 填写被寻人的详细信息：姓名、性别、失踪年龄、失踪日期、失踪地点、身高、体重、血型等\n3. 描述体貌特征、衣着描述、特殊特征（胎记、疤痕等）、失踪原因\n4. 填写您的联系方式和联系人信息\n5. 可以上传被寻人的照片（支持多张）\n6. 点击'发布寻亲信息'按钮提交\n7. 提交后请等待管理员审核，审核通过后信息将在网站上展示\n\n提示：信息越详细，找到亲人的概率越大。";
        }

        // 如何提供线索
        if (question.contains("如何") && (question.contains("线索") || question.contains("提供"))) {
            return "提供线索的步骤如下：\n1. 在寻亲信息详情页找到您想提供线索的寻亲信息\n2. 点击'提供线索'按钮\n3. 填写线索内容，尽可能详细描述您所知道的信息\n4. 您可以选择匿名提交或实名提交（实名提交需要填写联系方式）\n5. 点击提交，线索将发送给管理员审核\n6. 审核通过后，寻亲者将能看到您提供的线索\n\n注意：请确保您提供的线索真实可靠，有助于寻亲者找到亲人。";
        }

        // 如何搜索
        if (question.contains("如何") && question.contains("搜索")) {
            return "搜索寻亲信息的方法：\n1. 在网站首页的搜索框中输入被寻人的姓名进行搜索\n2. 也可以使用高级筛选功能，按性别、失踪地点、失踪日期等条件筛选\n3. 搜索结果会展示所有已审核通过的寻亲信息\n4. 点击任意结果可以查看详细信息，包括照片、体貌特征、联系方式等\n5. 您还可以按热门程度查看最受关注的寻亲信息";
        }

        // 如何联系
        if (question.contains("如何") && question.contains("联系")) {
            return "在每一条寻亲信息的详情页中，您都可以找到发布者填写的联系信息，包括：\n- 联系人姓名\n- 联系电话\n- 联系邮箱\n\n请直接通过这些方式与寻亲者或其家属取得联系。如果您希望保护隐私，也可以先通过网站提供线索，等待对方联系您。";
        }

        // 成功案例
        if (question.contains("成功案例") || question.contains("团聚") || (question.contains("找到") && question.contains("人"))) {
            return "本网站致力于帮助失散家庭团圆，目前已帮助多个家庭成功找到失散的亲人。您可以在网站'成功案例'栏目中查看：\n- 详细的寻亲成功故事\n- 寻亲过程中的经验和技巧\n- 团聚家庭的感人瞬间\n\n每个成功案例都展示了坚持寻亲的重要性，也证明了本网站寻亲模式的有效性。";
        }

        // 审核时间
        if ((question.contains("审核") || question.contains("多久")) && question.contains("通过")) {
            return "寻亲信息提交后，管理员通常会在1-3个工作日内完成审核。如果审核通过，您的信息将在网站上展示；如果被拒绝，您会收到拒绝原因，可以根据原因修改后重新提交。\n\n审核期间请耐心等待，您也可以在'我的寻亲信息'中查看审核状态。";
        }

        // 修改编辑
        if (question.contains("修改") || question.contains("编辑")) {
            return "如果您需要修改已发布的寻亲信息：\n1. 登录后进入'我的寻亲信息'页面\n2. 找到需要修改的寻亲信息，点击'编辑'按钮\n3. 在弹出的编辑窗口中修改信息\n4. 提交修改后，信息将重新进入审核流程\n5. 审核通过后修改生效\n\n提示：如果信息已审核通过，修改后需要重新审核，请确保修改内容的准确性。";
        }

        // 删除
        if (question.contains("删除")) {
            return "如果您需要删除已发布的寻亲信息：\n1. 登录后进入'我的寻亲信息'页面\n2. 找到需要删除的寻亲信息\n3. 点击'删除'按钮并确认\n4. 删除后该信息将不再在网站上展示\n\n注意：删除操作不可撤销，请谨慎操作。";
        }

        // 注册登录账号
        if ((question.contains("注册") || question.contains("登录")) && !question.contains("志愿者")) {
            return "账号相关操作：\n1. 注册：点击首页'注册'按钮，填写用户名、密码和基本信息完成注册\n2. 登录：使用注册的账号和密码登录\n3. 找回密码：如忘记密码，可联系管理员协助处理\n\n注册后您可以发布寻亲信息、提供线索、参与社区讨论等。";
        }

        // 隐私保护
        if (question.contains("匿名") || question.contains("隐私") || question.contains("保密")) {
            return "本网站高度重视用户隐私保护：\n1. 提供线索时，您可以选择匿名提交，寻亲者将看不到您的个人信息\n2. 您的个人联系方式仅在您实名提交线索时才会被对方看到\n3. 网站会对所有用户信息进行加密存储\n4. 我们严格遵守相关法律法规，保护用户数据安全\n5. 您可以在个人中心查看和管理您的个人信息";
        }

        return null;
    }

    private String getRegionAnswer(String region) {
        List<Map<String, Object>> results = searchByRegion(region);
        if (results.isEmpty()) {
            return "目前在" + region + "地区没有找到相关的寻亲信息。您可以尝试其他地区，或发布新的寻亲信息。";
        } else {
            StringBuilder answer = new StringBuilder("在" + region + "地区找到以下寻亲信息：\n\n");
            int count = 0;
            for (Map<String, Object> person : results) {
                if (count >= 5) break;
                answer.append("姓名：").append(person.get("name"))
                      .append("，性别：").append(person.get("gender"))
                      .append("，年龄：").append(person.get("age"))
                      .append("，失踪地点：").append(person.get("missingLocation"))
                      .append("，失踪时间：").append(person.get("missingDate"))
                      .append("\n");
                count++;
            }
            if (results.size() > 5) {
                answer.append("...等共").append(results.size()).append("条信息");
            }
            return answer.toString();
        }
    }

    private String getAIAnswer(String question) {
        // 创建带超时的 RestTemplate（5秒连接超时，10秒读取超时）
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        RestTemplate timeoutRestTemplate = new RestTemplate(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + deepseekApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");

        List<Map<String, Object>> messages = new ArrayList<>();

        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个专业的寻亲网站AI助手，系统名称是'寻亲网'。你的职责是帮助用户寻找失踪的亲人。请严格按照以下规则回答：\n\n1. 回答必须与本寻亲网站系统相关，包括但不限于：寻亲信息发布、线索提供、志愿者服务、成功案例、账号管理等\n2. 如果用户的问题与寻亲无关，请委婉地引导回到寻亲主题\n3. 回答要友好、温暖、有同理心，理解寻亲者的急切心情\n4. 提供专业、实用的建议，帮助用户更有效地寻亲\n5. 如果用户问到关于本网站的具体功能，请根据网站实际功能回答\n6. 可以向用户推荐网站的以下功能：发布寻亲信息、搜索失踪人员、提供线索、查看成功案例、申请成为志愿者\n7. 对于不确定的信息，不要编造，建议用户联系网站管理员获取准确信息\n8. 回答要简洁明了，使用中文，避免过于复杂的表述");
        messages.add(systemMessage);

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", question);
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 500);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // 使用线程池进行异步调用，设置总超时时间为12秒
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            try {
                ResponseEntity<String> responseEntity = timeoutRestTemplate.postForEntity(deepseekApiUrl, requestEntity, String.class);
                JsonNode rootNode = objectMapper.readTree(responseEntity.getBody());
                JsonNode choicesNode = rootNode.get("choices");
                if (choicesNode != null && choicesNode.isArray() && choicesNode.size() > 0) {
                    JsonNode messageNode = choicesNode.get(0).get("message");
                    if (messageNode != null) {
                        JsonNode contentNode = messageNode.get("content");
                        if (contentNode != null) {
                            return contentNode.asText();
                        }
                    }
                }
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        try {
            String result = future.get(12, TimeUnit.SECONDS);
            executor.shutdown();
            if (result != null) {
                return result;
            }
        } catch (TimeoutException e) {
            future.cancel(true);
            executor.shutdown();
            System.err.println("AI API调用超时: " + e.getMessage());
            return "抱歉，AI服务响应超时，请稍后再试。您也可以联系网站管理员获取帮助。";
        } catch (Exception e) {
            future.cancel(true);
            executor.shutdown();
            System.err.println("AI API调用失败: " + e.getMessage());
        }

        return "抱歉，我暂时无法回答您的问题，请稍后再试，或者您也可以联系网站管理员获取帮助。";
    }
}