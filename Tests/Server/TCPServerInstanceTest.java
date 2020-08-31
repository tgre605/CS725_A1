package Server;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class TCPServerInstanceTest {

    @Test
    void mode() {
    }

    @Test
    void user() throws Exception {
        Socket socket = new Socket();
        TCPServerInstance test = new TCPServerInstance(socket);
        assertEquals(test.user("test4"), "!test4 logged in");
        TCPServerInstance test2 = new TCPServerInstance(socket);
        assertEquals(test2.user("userID"), "+User-id valid, send account and password");
    }

    @Test
    void acct() {
    }

    @Test
    void pass() {
    }

    @Test
    void type() {
    }

    @Test
    void list() {
    }

    @Test
    void cdir() {
    }

    @Test
    void kill() {
    }

    @Test
    void name() {
    }

    @Test
    void tobe() {
    }

    @Test
    void retr() {
    }

    @Test
    void send() {
    }
}