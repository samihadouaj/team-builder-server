package com.nexters.teambuilder.user.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.nexters.teambuilder.user.api.dto.UserRequest;
import com.nexters.teambuilder.user.domain.User;
import com.nexters.teambuilder.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "${service.api-server}", uriPort = 80)
@WebMvcTest(value = UserController.class, secure = false)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User user;

    private ObjectMapper mapper;

    private FieldDescriptor[] userResposneDescription = new FieldDescriptor[]{
            fieldWithPath("uuid").description("user uuid"),
            fieldWithPath("id").description("아이디"),
            fieldWithPath("name").description("user 이름"),
            fieldWithPath("term").description("user 기수"),
            fieldWithPath("role").description("user 권한 {ROLE_ADMIN, ROLE_USER}"),
            fieldWithPath("position").description("user Position {DESIGNER, DEVELOPER}"),
            fieldWithPath("createdAt").description("user 가입 일자")
    };

    private FieldDescriptor[] userRequesteDescription = new FieldDescriptor[]{
            fieldWithPath("id").description("아이디"),
            fieldWithPath("password").description("비밀번호"),
            fieldWithPath("name").description("user 이름"),
            fieldWithPath("term").description("user 기수"),
            fieldWithPath("role").description("user 권한 {ROLE_ADMIN, ROLE_USER}"),
            fieldWithPath("position").description("user Position {DESIGNER, DEVELOPER}"),
    };

    @BeforeEach
    void setUp() {
        user = new User("originman", "password1212", "kiwon",
                13, User.Role.ROLE_USER, User.Position.DEVELOPER);

        mapper = new ObjectMapper();
    }

    @Test
    void signUp() throws Exception {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("id", "originman");
        input.put("password", "password1212");
        input.put("name", "kiwon");
        input.put("term", 13);
        input.put("role", "ROLE_USER");
        input.put("position", "DEVELOPER");

        given(userService.createUser(any(UserRequest.class))).willReturn(user);

        this.mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value("originman"))
                .andExpect(jsonPath("name").value("kiwon"))
                .andExpect(jsonPath("term").value(13))
                .andExpect(jsonPath("role").value("ROLE_USER"))
                .andExpect(jsonPath("position").value("DEVELOPER"))
                .andDo(document("users/post-signUp",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        requestFields(userRequesteDescription),
                        responseFields(userResposneDescription)));
    }

    @Test
    void signIn() throws Exception {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("id", "originman");
        input.put("password", "password1212");

        given(userService.logIn(anyString(), anyString())).willReturn(ImmutableMap.of("accessToken", "access token"));

        this.mockMvc.perform(post("/users/sign-in")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("id", "originman'")
                .param("password", "password1212'"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("accessToken").value("access token"))
                .andDo(document("users/post-signIn",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("id").description("user 아이디"),
                                parameterWithName("password").description("user 비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").description("access token for user"))));
    }
}
