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
        String reply = "!test4 logged in" + "\n";
        assertEquals(test.user("test4"), reply);
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