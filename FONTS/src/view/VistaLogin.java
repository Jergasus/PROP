package view;

import domini.controller.CtrlPresentacion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class VistaLogin extends JPanel {

    private final CtrlPresentacion ctrl;
    private final JTextField tfUser  = new JTextField(20);
    private final JPasswordField tfPass = new JPasswordField(20);
    private final JLabel lblStatus   = new JLabel(" ");

    public VistaLogin(CtrlPresentacion ctrl) {
        this.ctrl = ctrl;
        setLayout(new GridBagLayout());
        setBackground(new Color(0x2B2D30));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(0x3C3F41));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x5294E2), 2),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("HIDATO", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(new Color(0x5294E2));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(0, 0, 20, 0);
        card.add(title, gbc);

        gbc.gridwidth = 1; gbc.insets = new Insets(6, 6, 6, 6);

        JLabel lUser = label("Usuario:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        card.add(lUser, gbc);
        style(tfUser);
        gbc.gridx = 1; gbc.weightx = 1;
        card.add(tfUser, gbc);

        JLabel lPass = label("Contraseña:");
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        card.add(lPass, gbc);
        style(tfPass);
        gbc.gridx = 1; gbc.weightx = 1;
        card.add(tfPass, gbc);

        JButton btnLogin    = button("Iniciar sesión",  new Color(0x5294E2));
        JButton btnRegister = button("Registrarse",     new Color(0x6AA84F));

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        card.add(btnLogin, gbc);
        gbc.gridx = 1;
        card.add(btnRegister, gbc);

        lblStatus.setForeground(new Color(0xFF6B6B));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        card.add(lblStatus, gbc);

        add(card);

        btnLogin.addActionListener((ActionEvent e)    -> actionLogin());
        btnRegister.addActionListener((ActionEvent e) -> actionRegister());
        tfPass.addActionListener((ActionEvent e)      -> actionLogin());
    }

    private void actionLogin() {
        String user = tfUser.getText().trim();
        String pass = new String(tfPass.getPassword());
        String err = ctrl.login(user, pass);
        if (err == null) { limpiar(); ctrl.mostrarMenuPrincipal(); }
        else lblStatus.setText(err);
    }

    private void actionRegister() {
        String user = tfUser.getText().trim();
        String pass = new String(tfPass.getPassword());
        String err = ctrl.register(user, pass);
        if (err == null) { limpiar(); ctrl.mostrarMenuPrincipal(); }
        else lblStatus.setText(err);
    }

    private void limpiar() {
        tfUser.setText("");
        tfPass.setText("");
        lblStatus.setText(" ");
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return l;
    }

    private void style(JTextField tf) {
        tf.setBackground(new Color(0x4A4D50));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x666666)),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        tf.setFont(new Font("SansSerif", Font.PLAIN, 14));
    }

    private JButton button(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
