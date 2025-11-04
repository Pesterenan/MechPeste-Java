package com.pesterenan.views;

import static com.pesterenan.views.MainGui.BTN_DIMENSION;
import static com.pesterenan.views.MainGui.centerDialogOnScreen;
import static com.pesterenan.views.MainGui.createMarginComponent;

import com.pesterenan.utils.VersionUtil;
import java.awt.Desktop;
import java.awt.Font;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class AboutJFrame extends JDialog implements UIMethods {

  private static final long serialVersionUID = 0L;
  private JLabel lblMechpeste;
  private JEditorPane editorPane;
  private JButton btnOk;

  public AboutJFrame() {
    initComponents();
    setupComponents();
    layoutComponents();
  }

  @Override
  public void initComponents() {
    // Labels:
    lblMechpeste = new JLabel("MechPeste - v." + VersionUtil.getVersion());
    editorPane = new JEditorPane();

    // Buttons:
    btnOk = new JButton("OK");
  }

  @Override
  public void setupComponents() {
    // Main Panel setup:
    setTitle("Sobre");
    setBounds(centerDialogOnScreen());
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    setResizable(false);
    setAlwaysOnTop(true);
    setModalityType(ModalityType.APPLICATION_MODAL);

    // Setting-up components:
    lblMechpeste.setFont(new Font("Trajan Pro", Font.BOLD, 18));
    lblMechpeste.setAlignmentX(CENTER_ALIGNMENT);

    editorPane.setContentType("text/html");
    editorPane.setText(
        "<html><body style='font-family: \"arial, helvetica, sans-serif\"; text-align: justify;'>"
            + "Esse app foi desenvolvido com o intuito de auxiliar o controle de naves<br>no game Kerbal Space Program.<br><br>"
            + "Não há garantias sobre o controle exato do app, portanto fique atento <br>"
            + "para retomar o controle quando necessário.<br><br>"
            + "Feito por: Renan Torres<br>"
            + "Visite meu canal no Youtube! - <a href=\"https://www.youtube.com/@Pesterenan\">https://www.youtube.com/@Pesterenan</a><br><br>"
            + "Gostou do app? Considere fazer uma doação!<br>"
            + "LivePix: <a href=\"https://livepix.gg/pesterenan\">https://livepix.gg/pesterenan</a><br><br>"
            + "Código fonte: <a href=\"https://github.com/Pesterenan/MechPeste-Java\">https://github.com/Pesterenan/MechPeste-Java</a>"
            + "</body></html>");
    editorPane.setEditable(false);
    editorPane.setOpaque(false);
    editorPane.addHyperlinkListener(
        new HyperlinkListener() {
          @Override
          public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
              try {
                Desktop.getDesktop().browse(e.getURL().toURI());
              } catch (IOException | URISyntaxException e1) {
                e1.printStackTrace();
              }
            }
          }
        });

    btnOk.addActionListener(
        e -> {
          this.dispose();
        });
    btnOk.setPreferredSize(BTN_DIMENSION);
    btnOk.setMaximumSize(BTN_DIMENSION);
    btnOk.setAlignmentX(CENTER_ALIGNMENT);
  }

  @Override
  public void layoutComponents() {
    JPanel pnlMain = new JPanel();
    pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
    pnlMain.setBorder(MainGui.MARGIN_BORDER_10_PX_LR);
    pnlMain.add(createMarginComponent(10, 10));
    pnlMain.add(lblMechpeste);
    pnlMain.add(createMarginComponent(10, 10));
    pnlMain.add(editorPane);
    pnlMain.add(Box.createVerticalGlue());
    pnlMain.add(btnOk);
    pnlMain.add(createMarginComponent(10, 10));

    getContentPane().add(pnlMain);
    pack();
    setVisible(true);
  }
}
