package com.nexters.teambuilder.session.api;

import static com.nexters.teambuilder.idea.domain.Idea.Type.IDEA;
import static com.nexters.teambuilder.session.domain.Period.PeriodType.IDEA_COLLECT;
import static com.nexters.teambuilder.tag.domain.Tag.Type.DEVELOPER;
import static java.time.ZonedDateTime.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexters.teambuilder.idea.api.dto.IdeaResponse;
import com.nexters.teambuilder.idea.api.dto.VotedIdeaResponse;
import com.nexters.teambuilder.idea.domain.Idea;
import com.nexters.teambuilder.idea.service.IdeaService;
import com.nexters.teambuilder.session.api.dto.SessionNumber;
import com.nexters.teambuilder.session.api.dto.SessionRequest;
import com.nexters.teambuilder.session.domain.Period;
import com.nexters.teambuilder.session.domain.Session;
import com.nexters.teambuilder.session.service.SessionService;
import com.nexters.teambuilder.tag.api.dto.TagResponse;
import com.nexters.teambuilder.tag.domain.Tag;
import com.nexters.teambuilder.tag.service.TagService;
import com.nexters.teambuilder.user.domain.User;
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
@WebMvcTest(value = SessionController.class, secure = false)
class SessionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionService sessionService;

    @MockBean
    private TagService tagService;

    @MockBean
    private IdeaService ideaService;

    private ObjectMapper mapper;

    private User user;

    private Session session;

    private FieldDescriptor[] baseResposneDescription = new FieldDescriptor[]{
            fieldWithPath("status").description("status code"),
            fieldWithPath("errorCode").description("error code, ?????? ????????? ?????? front ?????? ??????????????? ??????"),
            fieldWithPath("data").description("respone data"),
    };

    private FieldDescriptor[] sessionRequestDescription = new FieldDescriptor[]{
            fieldWithPath("logoImageUrl").description("?????? ?????? ????????? url")
                    .attributes(key("constraints").value("??????, Not Empty")),
            fieldWithPath("teamBuildingMode").description("????????? ?????? ????????? ???????????? ????????? ??????"),
            fieldWithPath("periods[]").description("????????? ?????? ??????????????? ??????")
                    .attributes(key("constraints").value("??????, Not Null")),
            fieldWithPath("periods[].periodType").description("????????? ?????? ???????????? {IDEA_COLLECT, IDEA_VOTE, IDEA_CHECK, TEAM_BUILDING}"),
            fieldWithPath("periods[].startDate").description("??? ????????? ?????? ?????? ??????"),
            fieldWithPath("periods[].endDate").description("??? ????????? ?????? ????????? ??????"),
            fieldWithPath("maxVoteCount").description("?????? ?????? ?????? ??????")
                    .attributes(key("constraints").value("default : 0")),
    };

    private FieldDescriptor[] sessionResposneDescription = new FieldDescriptor[]{
            fieldWithPath("sessionId").description("session id"),
            fieldWithPath("sessionNumber").description("?????? ??????"),
            fieldWithPath("sessionNumbers[]").description("?????? ?????? ?????????"),
            fieldWithPath("sessionNumbers[].sessionNumber").description("?????? ????????????"),
            fieldWithPath("logoImageUrl").description("?????? ?????? ????????? url"),
            fieldWithPath("teamBuildingMode").description("????????? ?????? ??????"),
            fieldWithPath("periods[]").description("????????? ?????? ??????????????? ??????"),
            fieldWithPath("periods[].periodType").description("????????? ?????? ???????????? {IDEA_COLLECT, IDEA_VOTE, IDEA_CHECK, TEAM_BUILDING}"),
            fieldWithPath("periods[].startDate").description("??? ????????? ?????? ????????????"),
            fieldWithPath("periods[].endDate").description("??? ????????? ?????? ????????? ??????"),
            fieldWithPath("periods[].now").description("?????? ???????????? ?????????????????? ??????"),
            fieldWithPath("tags[]").description("tag ??????"),
            fieldWithPath("tags[].tagId").description("tag ?????????"),
            fieldWithPath("tags[].name").description("tag ??????"),
            fieldWithPath("tags[].type").description("tag ?????? {DEVELOPER, DESIGNER}"),
            fieldWithPath("maxVoteCount").description("?????? ?????? ?????? ???"),
            fieldWithPath("ideas[]").description("Idea ??????"),
            fieldWithPath("ideas[].ideaId").description("Idea id"),
            fieldWithPath("ideas[].sessionId").description("??????????????? ????????? session(??????) id"),
            fieldWithPath("ideas[].title").description("???????????? ??????"),
            fieldWithPath("ideas[].content").description("???????????? ??????"),
            fieldWithPath("ideas[].author").description("????????? ??????"),
            fieldWithPath("ideas[].author").description("????????? ??????"),
            fieldWithPath("ideas[].author.uuid").description("user uuid"),
            fieldWithPath("ideas[].author.id").description("?????????"),
            fieldWithPath("ideas[].author.name").description("user ??????"),
            fieldWithPath("ideas[].author.nextersNumber").description("user ??????"),
            fieldWithPath("ideas[].author.email").description("user email"),
            fieldWithPath("ideas[].author.role").description("user ?????? {ROLE_ADMIN, ROLE_USER}"),
            fieldWithPath("ideas[].author.position").description("user Position {DESIGNER, DEVELOPER}"),
            fieldWithPath("ideas[].author.activated").description("user ????????? ??????"),
            fieldWithPath("ideas[].author.voteCount").description("user ???????????? ?????? ??????"),
            fieldWithPath("ideas[].author.voted").description("user ?????? ??????"),
            fieldWithPath("ideas[].author.submitIdea").description("user ???????????? ?????? ??????"),
            fieldWithPath("ideas[].author.hasTeam").description("user ??? ?????? ??????"),
            fieldWithPath("ideas[].author.createdAt").description("user ?????? ??????"),
            fieldWithPath("ideas[].tags[]").description("tag ??????"),
            fieldWithPath("ideas[].tags[].tagId").description("tag ??????"),
            fieldWithPath("ideas[].tags[].name").description("tag ??????"),
            fieldWithPath("ideas[].tags[].type").description("tag ?????? {DEVELOPER, DESIGNER}"),
            fieldWithPath("ideas[].file").description("???????????? url"),
            fieldWithPath("ideas[].type").description("???????????? ?????? {IDEA, NOTICE}"),
            fieldWithPath("ideas[].selected").description("???????????? ?????? ??????"),
            fieldWithPath("ideas[].favorite").description("???????????? ???????????? ??????"),
            fieldWithPath("ideas[].orderNumber").description("?????? ??????").type(NUMBER).optional(),
            fieldWithPath("ideas[].voteNumber").description("?????? ???"),
            fieldWithPath("ideas[].createdAt").description("?????? ??????"),
            fieldWithPath("ideas[].updatedAt").description("???????????? ??????"),
            fieldWithPath("ideas[].members").description("??? ?????? ??????"),
            fieldWithPath("ideas[].members").description("??? ?????? ??????"),
            fieldWithPath("ideas[].members[].uuid").description("??? ?????? uuid"),
            fieldWithPath("ideas[].members[].id").description("??? ?????? id"),
            fieldWithPath("ideas[].members[].name").description("??? ?????? name"),
            fieldWithPath("ideas[].members[].nextersNumber").description("??? ?????? ???????????? ??????"),
            fieldWithPath("ideas[].members[].position").description("??? ?????? ????????? {DESIGNER, DEVELOPER}"),
            fieldWithPath("ideas[].members[].hasTeam").description("??? ????????? ?????? '??????'?????? ???????????? ?????? ????????????????????? ??????"),
            fieldWithPath("votedIdeas[]").description("Idea ??????"),
            fieldWithPath("votedIdeas[].ideaId").description("Idea id"),
            fieldWithPath("votedIdeas[].title").description("Idea ??????"),
    };

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        user = new User("originman", "password1212", "kiwon",
                13, User.Role.ROLE_USER, User.Position.DEVELOPER, "originman@nexter.com");

        session = mock(Session.class);
        given(session.getSessionId()).willReturn(1);
        given(session.getSessionNumber()).willReturn(1);
        given(session.isTeamBuildingMode()).willReturn(false);
        given(session.getPeriods()).willReturn(Arrays.asList(new Period(IDEA_COLLECT, now(), now())));
        given(session.getLogoImageUrl()).willReturn("https://logo/image/url");
        given(session.getMaxVoteCount()).willReturn(1);
    }

    @Test
    void get_Session() throws Exception {
        List<TagResponse> tags = IntStream.range(1, 3).mapToObj(i -> new Tag("?????????", DEVELOPER))
                .map(TagResponse::of).collect(Collectors.toList());

        List<SessionNumber> sessionNumbers = IntStream.range(1, 3).mapToObj(i -> new SessionNumber(i))
                .collect(Collectors.toList());

        List<IdeaResponse> ideas = IntStream.range(1, 3).mapToObj(i -> new Idea(session, "???????????? ??? ?????????",
                "???????????? ?????????????????? ????????????", user, "https://file.url",
                IDEA, Arrays.asList(new Tag("ios ?????????", DEVELOPER))))
                .map(IdeaResponse::of).collect(Collectors.toList());

        List<VotedIdeaResponse> votedIdeas = IntStream.range(1, 4)
                .mapToObj(i -> new VotedIdeaResponse(i, "title" + i))
                .collect(Collectors.toList());

        given(sessionService.getSession(anyInt())).willReturn(session);
        given(ideaService.getIdeaListBySessionId(anyInt(), any(User.class))).willReturn(ideas);
        given(ideaService.votedIdeas(any(User.class), anyInt())).willReturn(votedIdeas);
        given(sessionService.sessionNumberList()).willReturn(sessionNumbers);
        given(tagService.getTagList()).willReturn(tags);

        this.mockMvc.perform(get("/apis/sessions/{sessionNumber}", 1)
                .header("Authorization", "Bearer " + "<access_token>"))
                .andExpect(status().isOk())
                .andDo(document("sessions/get-session",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("sessionNumber").description("?????? ??????")
                                        .attributes(key("constraints").value("Not Null"))),
                        responseFields(baseResposneDescription)
                                .andWithPrefix("data.", sessionResposneDescription)
                ));
    }

    @Test
    void latest_Session() throws Exception {
        List<TagResponse> tags = IntStream.range(1, 3).mapToObj(i -> new Tag("?????????", DEVELOPER))
                .map(TagResponse::of).collect(Collectors.toList());

        List<SessionNumber> sessionNumbers = IntStream.range(1, 3).mapToObj(i -> new SessionNumber(i))
                .collect(Collectors.toList());

        List<IdeaResponse> ideas = IntStream.range(1, 3).mapToObj(i -> new Idea(session, "???????????? ??? ?????????",
                "???????????? ?????????????????? ????????????", user, "https://file.url",
                IDEA, Arrays.asList(new Tag("ios ?????????", DEVELOPER))))
                .map(IdeaResponse::of).collect(Collectors.toList());

        List<VotedIdeaResponse> votedIdeas = IntStream.range(1, 4)
                .mapToObj(i -> new VotedIdeaResponse(i, "title" + i))
                .collect(Collectors.toList());

        given(sessionService.getLatestSession()).willReturn(session);
        given(ideaService.getIdeaListBySessionId(anyInt(), any(User.class))).willReturn(ideas);
        given(ideaService.votedIdeas(any(User.class), anyInt())).willReturn(votedIdeas);
        given(sessionService.sessionNumberList()).willReturn(sessionNumbers);
        given(tagService.getTagList()).willReturn(tags);

        this.mockMvc.perform(get("/apis/sessions/latest")
                .header("Authorization", "Bearer " + "<access_token>"))
                .andExpect(status().isOk())
                .andDo(document("sessions/latest-session",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        responseFields(baseResposneDescription)
                                .andWithPrefix("data.", sessionResposneDescription)
                ));
    }

    @Test
    void create_Session() throws Exception {

        List<TagResponse> tags = IntStream.range(1, 3).mapToObj(i -> new Tag("?????????", DEVELOPER))
                .map(TagResponse::of).collect(Collectors.toList());

        List<SessionNumber> sessionNumbers = IntStream.range(1, 3).mapToObj(i -> new SessionNumber(i))
                .collect(Collectors.toList());

        List<IdeaResponse> ideas = IntStream.range(1, 3).mapToObj(i -> new Idea(session, "???????????? ??? ?????????",
                "???????????? ?????????????????? ????????????", user, "https://file.url",
                IDEA, Arrays.asList(new Tag("ios ?????????", DEVELOPER))))
                .map(IdeaResponse::of).collect(Collectors.toList());

        List<VotedIdeaResponse> votedIdeas = IntStream.range(1, 4)
                .mapToObj(i -> new VotedIdeaResponse(i, "title" + i))
                .collect(Collectors.toList());

        given(sessionService.createSession(any(SessionRequest.class))).willReturn(session);
        given(ideaService.getIdeaListBySessionId(anyInt(), any(User.class))).willReturn(ideas);
        given(ideaService.votedIdeas(any(User.class), anyInt())).willReturn(votedIdeas);
        given(sessionService.sessionNumberList()).willReturn(sessionNumbers);
        given(ideaService.votedIdeas(any(User.class), anyInt())).willReturn(votedIdeas);
        given(tagService.getTagList()).willReturn(tags);

        Map<String, Object> period = new LinkedHashMap<>();
        period.put("periodType", IDEA_COLLECT);
        period.put("startDate", now().toOffsetDateTime().toString());
        period.put("endDate", now().toOffsetDateTime().toString());

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("logoImageUrl", "https://logo/image.url");
        input.put("teamBuildingMode", false);
        input.put("periods", Arrays.asList(period));
        input.put("maxVoteCount", 3);

        this.mockMvc.perform(post("/apis/sessions")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(input))
                .header("Authorization", "Bearer " + "<access_token>"))
                .andExpect(status().isOk())
                .andDo(document("sessions/post-session",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        requestFields(sessionRequestDescription),
                        responseFields(baseResposneDescription)
                                .andWithPrefix("data.", sessionResposneDescription)
                ));
    }

    @Test
    void update_Session() throws Exception {

        List<TagResponse> tags = IntStream.range(1, 3).mapToObj(i -> new Tag("?????????", DEVELOPER))
                .map(TagResponse::of).collect(Collectors.toList());

        List<SessionNumber> sessionNumbers = IntStream.range(1, 3).mapToObj(i -> new SessionNumber(i))
                .collect(Collectors.toList());

        List<IdeaResponse> ideas = IntStream.range(1, 3).mapToObj(i -> new Idea(session, "???????????? ??? ?????????",
                "???????????? ?????????????????? ????????????", user, "https://file.url",
                IDEA, Arrays.asList(new Tag("ios ?????????", DEVELOPER))))
                .map(IdeaResponse::of).collect(Collectors.toList());

        List<VotedIdeaResponse> votedIdeas = IntStream.range(1, 4)
                .mapToObj(i -> new VotedIdeaResponse(i, "title" + i))
                .collect(Collectors.toList());


        given(sessionService.updateSession(anyInt(), any(SessionRequest.class))).willReturn(session);
        given(ideaService.getIdeaListBySessionId(anyInt(), any(User.class))).willReturn(ideas);
        given(ideaService.votedIdeas(any(User.class), anyInt())).willReturn(votedIdeas);
        given(sessionService.sessionNumberList()).willReturn(sessionNumbers);
        given(tagService.getTagList()).willReturn(tags);

        Map<String, Object> period = new LinkedHashMap<>();
        period.put("periodType", IDEA_COLLECT);
        period.put("startDate", now().toOffsetDateTime().toString());
        period.put("endDate", now().toOffsetDateTime().toString());

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("logoImageUrl", "https://logo/image.url");
        input.put("teamBuildingMode", false);
        input.put("periods", Arrays.asList(period));
        input.put("maxVoteCount", 3);

        this.mockMvc.perform(put("/apis/sessions/{sessionNumber}", 1)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(input))
                .header("Authorization", "Bearer " + "<access_token>"))
                .andExpect(status().isOk())
                .andDo(document("sessions/update-session",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("sessionNumber").description("?????? ??????")
                                        .attributes(key("constraints").value("Not Null"))),
                        requestFields(sessionRequestDescription),
                        responseFields(baseResposneDescription)
                                .andWithPrefix("data.", sessionResposneDescription)
                ));
    }

    @Test
    void delete_session() throws Exception {
        this.mockMvc.perform(delete("/apis/sessions/{sessionNumber}", 1)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + "<access_token>"))
                .andExpect(status().isOk())
                .andDo(document("sessions/delete-session",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("sessionNumber").description("?????? ??????")
                                        .attributes(key("constraints").value("Not Null")))
                ));
    }
}