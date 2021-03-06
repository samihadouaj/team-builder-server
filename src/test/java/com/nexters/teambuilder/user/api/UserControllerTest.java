package com.nexters.teambuilder.user.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexters.teambuilder.user.api.dto.SignInResponse;
import com.nexters.teambuilder.user.api.dto.UserRequest;
import com.nexters.teambuilder.user.api.dto.UserResponse;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.nexters.teambuilder.user.domain.User.Position.DEVELOPER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private FieldDescriptor[] baseResponseDescription = new FieldDescriptor[]{
            fieldWithPath("status").description("status code"),
            fieldWithPath("errorCode").description("error code, ?????? ????????? ?????? front ?????? ??????????????? ??????"),
            fieldWithPath("data").description("respone data"),
    };

    private FieldDescriptor[] signInResponseDescription = new FieldDescriptor[]{
            fieldWithPath("accessToken").description("access token for user"),
            fieldWithPath("role").description("user role")
    };

    private FieldDescriptor[] userResponseDescription = new FieldDescriptor[]{
            fieldWithPath("uuid").description("user uuid"),
            fieldWithPath("id").description("?????????"),
            fieldWithPath("name").description("user ??????"),
            fieldWithPath("nextersNumber").description("user ??????"),
            fieldWithPath("email").description("user email"),
            fieldWithPath("activated").description("user ????????? ??????"),
            fieldWithPath("position").description("user Position {DESIGNER, DEVELOPER}"),
            fieldWithPath("role").description("user ?????? {ROLE_ADMIN, ROLE_USER}"),
            fieldWithPath("voteCount").description("user ???????????? ?????? ??????"),
            fieldWithPath("voted").description("user ?????? ??????"),
            fieldWithPath("submitIdea").description("user ???????????? ?????? ??????"),
            fieldWithPath("hasTeam").description("user ??? ?????? ??????"),
            fieldWithPath("createdAt").description("user ?????? ??????"),
    };

    private FieldDescriptor[] userRequestDescription = new FieldDescriptor[]{
            fieldWithPath("id").description("?????????"),
            fieldWithPath("password").description("????????????"),
            fieldWithPath("name").description("user ??????"),
            fieldWithPath("nextersNumber").description("user ??????"),
            fieldWithPath("email").description("user email"),
            fieldWithPath("role").description("user ?????? {ROLE_ADMIN, ROLE_USER}"),
            fieldWithPath("position").description("user Position {DESIGNER, DEVELOPER}"),
            fieldWithPath("authenticationCode").description("??????????????? ?????? ????????????"),
    };

    private FieldDescriptor[] userUpdateRequestDescription = new FieldDescriptor[]{
            fieldWithPath("nowPassword").description("?????? ????????????"),
            fieldWithPath("newPassword").description("????????? ???????????? (null ?????? ???????????? ?????? ??????)"),
            fieldWithPath("position").description("user Position {DESIGNER, DEVELOPER} (null ?????? ?????? ??????)"),
    };

    @BeforeEach
    void setUp() {
        user = new User("originman", "password1212", "kiwon",
                13, User.Role.ROLE_USER, User.Position.DEVELOPER, "originman@nexter.com");

        mapper = new ObjectMapper();
    }

    @Test
    void checkId() throws Exception {
        given(userService.isIdUsable(anyString())).willReturn(true);

        this.mockMvc.perform(get("/users/check-id").param("id", "test1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.isIdUsable").value(true))
                .andDo(document("users/get-checkId",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("id").description("??????????????? ????????? ID")
                        ),
                        responseFields(baseResponseDescription)
                                .andWithPrefix("data.",
                                        fieldWithPath("isIdUsable").description("????????? ?????? ????????????"))));
    }

    @Test
    void signUp() throws Exception {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("id", "originman");
        input.put("password", "password1212");
        input.put("name", "kiwon");
        input.put("nextersNumber", 13);
        input.put("role", "ROLE_USER");
        input.put("position", "DEVELOPER");
        input.put("email", "originman@nexter.com");
        input.put("authenticationCode", 12345);

        given(userService.createUser(any(UserRequest.class))).willReturn(UserResponse.of(user));

        this.mockMvc.perform(post("/users/sign-up")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").value("originman"))
                .andExpect(jsonPath("data.name").value("kiwon"))
                .andExpect(jsonPath("data.nextersNumber").value(13))
                .andExpect(jsonPath("data.role").value("ROLE_USER"))
                .andExpect(jsonPath("data.position").value("DEVELOPER"))
                .andDo(document("users/post-signUp",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        requestFields(userRequestDescription),
                        responseFields(baseResponseDescription)
                                .andWithPrefix("data.", userResponseDescription)));
    }

    @Test
    void signIn() throws Exception {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("id", "originman");
        input.put("password", "password1212");

        given(userService.signIn(anyString(), anyString()))
                .willReturn(new SignInResponse("access token", User.Role.ROLE_USER));

        this.mockMvc.perform(post("/users/sign-in")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("id", "originman'")
                .param("password", "password1212'"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.accessToken").value("access token"))
                .andDo(document("users/post-signIn",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("id").description("user ?????????"),
                                parameterWithName("password").description("user ????????????")
                        ),
                        responseFields(baseResponseDescription)
                                        .andWithPrefix("data.", signInResponseDescription)));
    }

    @Test
    void list() throws Exception {
        List<UserResponse> users = IntStream.range(1, 11).mapToObj(i -> {
            user = new User("originman" + i, "password1212", "kiwon",
                    13, User.Role.ROLE_USER, User.Position.DEVELOPER, "originman@nexter.com");
            user.activate();

            return UserResponse.of(user);
        }).collect(Collectors.toList());

        given(userService.userList()).willReturn(users);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/apis/users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + "<access_token>"))
                .andExpect(status().isOk())
                .andDo(document("users/list-users",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        responseFields(baseResponseDescription)
                                .andWithPrefix("data.[].", userResponseDescription)));
    }

    @Test
    void activatedUserList() throws Exception {
        List<UserResponse> users = IntStream.range(1, 4).mapToObj(i -> {
            user = new User("originman" + i, "password1212", "kiwon",
                    13, User.Role.ROLE_USER, User.Position.DEVELOPER, "originman@nexter.com");
            user.activate();

            return UserResponse.of(user);
        }).collect(Collectors.toList());

        given(userService.activatedUserList()).willReturn(users);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/apis/activated/users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + "<access_token>"))
                .andExpect(status().isOk())
                .andDo(document("users/list-activated-users",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        responseFields(baseResponseDescription)
                                .andWithPrefix("data.[].", userResponseDescription)));
    }

    @Test
    void updateUser() throws Exception {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("nowPassword", "1212");
        input.put("newPassword", "3434");
        input.put("position", DEVELOPER);

        this.mockMvc.perform(put("/apis/users")
                .header("Authorization", "Bearer " + "<access_token>")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andDo(document("users/put-user",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        requestFields(userUpdateRequestDescription),
                        responseFields(baseResponseDescription)));
    }

    @Test
    void activate() throws Exception {
        List<UserResponse> users = IntStream.range(1, 11).mapToObj(i -> {
            user = new User("originman" + i, "password1212", "kiwon",
                    13, User.Role.ROLE_USER, User.Position.DEVELOPER, "originman@nexter.com");
            user.activate();

            return UserResponse.of(user);
        }).collect(Collectors.toList());

        this.mockMvc.perform(put("/apis/users/{uuid}/activate", "aaaa-aaa-aaaa-aaaa")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + "<access_token>"))
                .andExpect(status().isOk())
                .andDo(document("users/put-activate",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("uuid").description("??????????????? user uuid")
                        ),
                        responseFields(baseResponseDescription)));
    }

    @Test
    void deactivate() throws Exception {
        List<UserResponse> users = IntStream.range(1, 11).mapToObj(i -> {
            user = new User("originman" + i, "password1212", "kiwon",
                    13, User.Role.ROLE_USER, User.Position.DEVELOPER, "originman@nexter.com");
            user.activate();

            return UserResponse.of(user);
        }).collect(Collectors.toList());

        this.mockMvc.perform(put("/apis/users/{uuid}/deactivate", "aaaa-aaa-aaaa-aaaa")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + "<access_token>"))
                .andExpect(status().isOk())
                .andDo(document("users/put-deactivate",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("uuid").description("?????????????????? user uuid")
                        ),
                        responseFields(baseResponseDescription)));
    }

    @Test
    void deactivateAll() throws Exception {
        List<UserResponse> users = IntStream.range(1, 11).mapToObj(i -> {
            user = new User("originman" + i, "password1212", "kiwon",
                    13, User.Role.ROLE_USER, User.Position.DEVELOPER, "originman@nexter.com");
            user.deactivate();

            return UserResponse.of(user);
        }).collect(Collectors.toList());

        given(userService.deactivateAllUsers()).willReturn(users);

        this.mockMvc.perform(put("/apis/users/deactivate/all")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + "<access_token>"))
                .andExpect(status().isOk())
                .andDo(document("users/put-deactivate-all",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        responseFields(baseResponseDescription)
                                .andWithPrefix("data.[].", userResponseDescription)));
    }

    @Test
    void dismissUsers() throws Exception {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("uuids", Arrays.asList("awa34er-adfg-ersaer-324aewr", "asdf342-avcxv-345ert-fhdgfh"));

        this.mockMvc.perform(put("/apis/users/dismiss")
                .content(mapper.writeValueAsString(input))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + "<access_token>"))
                .andExpect(status().isOk())
                .andDo(document("users/put-dismiss",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization")
                                        .description("Bearer oAuth2 access_token,"
                                                + " admin????????? ???????????? error ?????? error code : 90007")),
                        requestFields(
                                fieldWithPath("uuids").description("??????????????? ???????????? uuid ??????")),
                        responseFields(baseResponseDescription)));
    }
}

