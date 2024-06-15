import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 Trabalho 1 - Sistemas Distribuídos
 Guilherme Medeiros da Cunha
 Gustavo Machado de Freitas
 */
public class ChatClient {

    String serverAddress;
    Scanner in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(50);
    // JTextArea passou a ser um JTextPane para suportar diferentes estilos de texto.
    JTextPane messagePane = new JTextPane();

    /**
     * Constructs the client by laying out the GUI and registering a listener with
     * the textfield so that pressing Return in the listener sends the textfield
     * contents to the server. Note however that the textfield is initially NOT
     * editable, and only becomes editable AFTER the client receives the
     * NAMEACCEPTED message from the server.
     */
    public ChatClient(String serverAddress) {
        this.serverAddress = serverAddress;

        textField.setEditable(false);
        messagePane.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        // O JScrollPane precisou ser iniciado fora do método add para permitir o redimensionamento.
        JScrollPane scrollPane = new JScrollPane(messagePane);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.pack();

        setMessageStyles();

        // Send on enter then clear to prepare for next message
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
    }

    private String getName() {
        return JOptionPane.showInputDialog(frame, "Choose a screen name:", "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    // Configura os diferentes estilos de mensagem.
    private void setMessageStyles() {
        Style userMessageStyle = messagePane.addStyle("Message", null);
        StyleConstants.setForeground(userMessageStyle, Color.BLACK);
        StyleConstants.setFontFamily(userMessageStyle, "Arial");
        StyleConstants.setFontSize(userMessageStyle, 12);

        Style systemMessageStyle = messagePane.addStyle("System", null);
        StyleConstants.setForeground(systemMessageStyle, Color.GREEN);
        StyleConstants.setFontFamily(systemMessageStyle, "Arial");
        StyleConstants.setFontSize(systemMessageStyle, 12);
    }

    private void run() throws IOException {
        try {
            var socket = new Socket(serverAddress, 59001);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            StyledDocument document = messagePane.getStyledDocument();

            while (in.hasNextLine()) {
                var line = in.nextLine();
                if (line.startsWith("SUBMITNAME")) {
                    out.println(getName());
                } else if (line.startsWith("NAMEACCEPTED")) {
                    this.frame.setTitle("Chatter - " + line.substring(13));
                    textField.setEditable(true);
                } else if (line.startsWith("MESSAGE")) {
                    document.insertString(document.getLength(), line.substring(8) + "\n", messagePane.getStyle("Message"));
                } else if (line.startsWith("SERVER")) {
                    document.insertString(document.getLength(), line.substring(7) + "\n", messagePane.getStyle("System"));
                }
            }
        } catch (BadLocationException e) {
            // Exception disparada caso o código não consiga adicionar uma mensagem no painel.
            e.printStackTrace();
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Pass the server IP as the sole command line argument");
            return;
        }
        var client = new ChatClient(args[0]);
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}