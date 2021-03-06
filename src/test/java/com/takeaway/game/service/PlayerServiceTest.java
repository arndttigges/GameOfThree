package com.takeaway.game.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestAttributes;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private RequestAttributes requestAttributes;
//    @Mock
//    PlayerRepository playerRepository;
    InvitationService playerService;

//    @BeforeEach
//    void init() {
//        playerService = new InvitationService(playerRepository);
//        RequestContextHolder.setRequestAttributes(requestAttributes);
//    }
//
//    @Test
//    void filterPlayerWithRunningGames() {
//        String sessionId = "SESSION-ID";
//        String playerId = "c";
//
//        List<Player> players = List.of(
//                Player.builder().id("a").games(List.of(Game.builder().status(GameStatus.READY).build())).build(),
//                Player.builder().id("b").games(List.of(Game.builder().status(GameStatus.WAITING).build())).build(),
//                Player.builder().id(playerId).games(List.of(Game.builder().status(GameStatus.FINISHED).build())).build()
//        );
//
//        when(playerRepository.findAll()).thenReturn(players);
//        when(requestAttributes.getSessionId()).thenReturn(sessionId);
//
//        List<Player> playerList = playerService.getInvitations();
//        assertEquals(1, playerList.size());
//        assertEquals(playerId, playerList.get(0).getId());
//
//        verify(playerRepository).findAll();
//        verify(requestAttributes, atLeastOnce()).getSessionId();
//    }
//
//    @Test
//    void showPlayerWithoutRunningGames() {
//        String sessionId = "SESSION-ID";
//
//        List<Player> players = List.of(
//                Player.builder().id("a").build()
//        );
//
//        when(playerRepository.findAll()).thenReturn(players);
//        when(requestAttributes.getSessionId()).thenReturn(sessionId);
//
//        List<Player> playerList = playerService.getInvitations();
//        assertEquals(1, playerList.size());
//
//        verify(playerRepository).findAll();
//        verify(requestAttributes).getSessionId();
//    }
//
//    @Test
//    void doNotSeeMyself() {
//        String sessionId = "SESSION-ID";
//
//        List<Player> players = List.of(
//          Player.builder().id(sessionId).build()
//        );
//
//        when(playerRepository.findAll()).thenReturn(players);
//        when(requestAttributes.getSessionId()).thenReturn(sessionId);
//
//        List<Player> playerList = playerService.getInvitations();
//        assertEquals(0, playerList.size());
//
//        verify(playerRepository).findAll();
//        verify(requestAttributes).getSessionId();
//    }
//
//    @Test
//    void doNotSavePlayerWithoutId() {
//        Player result = playerService.createPlayer(null, null);
//        assertNull(result);
//        verify(playerRepository, times(0)).save(any());
//    }
//
//    @Test
//    void createPlayerTest() {
//        String playerId = "foo";
//        String playerName = "bar";
//
//        when(playerRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
//        Player result = playerService.createPlayer(playerId, playerName);
//
//        assertAll(
//                () -> assertEquals(playerId, result.getId()),
//                () -> assertEquals(playerName, result.getName())
//        );
//
//        verify(playerRepository).save(any());
//    }


}