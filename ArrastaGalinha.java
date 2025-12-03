package arrastagalinha;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class ArrastaGalinha {

    // ===== INTERFACE =====
    interface Salvamento {
        void salvarPontuacao();
    }

    // ===== CLASSE ABSTRATA =====
    abstract static class ElementoVisual {
        int x, y, largura, altura;
        Color cor;

        public ElementoVisual(int x, int y, int l, int a, Color cor) {
            this.x = x;
            this.y = y;
            this.largura = l;
            this.altura = a;
            this.cor = cor;
        }

        public abstract void desenhar(Graphics g);

        public boolean contemPonto(int mouseX, int mouseY) {
            return (mouseX >= x && mouseX <= x + largura &&
                    mouseY >= y && mouseY <= y + altura);
        }

        public boolean estaDentroDe(ElementoVisual outro) {
            return (this.x > outro.x &&
                    this.x + this.largura < outro.x + outro.largura &&
                    this.y > outro.y &&
                    this.y + this.altura < outro.y + outro.altura);
        }
    }

    // ===== GALINHEIRO =====
    static class Galinheiro extends ElementoVisual {

        String equacao = "";

        public Galinheiro(int x, int y) {
            super(x, y, 150, 150, new Color(139, 69, 19));
        }

        @Override
        public void desenhar(Graphics g) {
            g.setColor(this.cor);
            g.fillRect(x, y, largura, altura);

            g.setColor(new Color(160, 82, 45));
            g.fillRect(x - 10, y - 20, largura + 20, 20);

            g.setColor(Color.WHITE);
            g.drawString("GALINHEIRO", x + 28, y + 40);

            // Equação
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString(equacao, x + 40, y + 100);
        }
    }

    // ===== GALINHA =====
    static class Galinha extends ElementoVisual {

        boolean salva = false;
        int inicioX, inicioY;

        public Galinha(int x, int y) {
            super(x, y, 40, 40, Color.YELLOW);
            inicioX = x;
            inicioY = y;
        }

        @Override
        public void desenhar(Graphics g) {
            g.setColor(salva ? Color.WHITE : Color.YELLOW);
            g.fillOval(x, y, largura, altura);

            g.setColor(Color.BLACK);
            g.fillOval(x + 25, y + 10, 5, 5);

            g.setColor(Color.RED);
            g.fillPolygon(new int[]{x + 30, x + 40, x + 30},
                    new int[]{y + 15, y + 20, y + 25}, 3);
        }
    }

    // ===== JOGO PRINCIPAL =====
    public static class JogoGalinheiro extends JFrame implements Salvamento {

        Galinheiro galinheiro;
        ArrayList<Galinha> listaGalinhas;
        Galinha galinhaSelecionada = null;

        int pontuacao = 0;
        JLabel textoPontos;

        // lógicas da equação
        int objetivo = 0;
        int salvasNestaRodada = 0;
        String equacaoAtual = "";

        public JogoGalinheiro() {

            setTitle("Galinheiro Matemático");
            setSize(800, 600);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(null);

            galinheiro = new Galinheiro(600, 200);

            listaGalinhas = new ArrayList<>();
            gerarGalinhas();

            gerarNovaEquacao();
            galinheiro.equacao = equacaoAtual;

            // ===== PAINEL =====
            JPanel painel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);

                    g.setColor(new Color(144, 238, 144));
                    g.fillRect(0, 0, 800, 600);

                    galinheiro.desenhar(g);

                    for (Galinha gali : listaGalinhas) {
                        gali.desenhar(g);
                    }
                }
            };
            painel.setBounds(0, 0, 800, 450);
            add(painel);

            // ===== PONTUAÇÃO =====
            textoPontos = new JLabel("Galinhas salvas: 0");
            textoPontos.setFont(new Font("Arial", Font.BOLD, 20));
            textoPontos.setBounds(30, 500, 250, 30);
            add(textoPontos);

            // ===== BOTÃO SALVAR =====
            JButton btnSalvar = new JButton("Salvar Pontos");
            btnSalvar.setBounds(600, 500, 150, 30);
            btnSalvar.addActionListener(e -> salvarPontuacao());
            add(btnSalvar);

            // ===== MOUSE =====
            MouseAdapter mouseHandler = new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    for (Galinha g : listaGalinhas) {
                        if (!g.salva && g.contemPonto(e.getX(), e.getY())) {
                            galinhaSelecionada = g;
                            break;
                        }
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (galinhaSelecionada != null) {
                        galinhaSelecionada.x = e.getX() - 20;
                        galinhaSelecionada.y = e.getY() - 20;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (galinhaSelecionada != null) {

                        if (galinhaSelecionada.estaDentroDe(galinheiro) && !galinhaSelecionada.salva) {

                            galinhaSelecionada.salva = true;
                            pontuacao++;
                            salvasNestaRodada++;

                            textoPontos.setText("Galinhas salvas: " + pontuacao);

                            // COMPLETOU O OBJETIVO
                            if (salvasNestaRodada == objetivo) {
                                JOptionPane.showMessageDialog(null,
                                        "Muito bem! Você completou " + objetivo + " galinhas!");

                                gerarNovaEquacao();
                                galinheiro.equacao = equacaoAtual;
                                gerarGalinhas();
                            }
                        }

                        galinhaSelecionada = null;
                        repaint();
                    }
                }
            };

            painel.addMouseListener(mouseHandler);
            painel.addMouseMotionListener(mouseHandler);

            setVisible(true);
            setLocationRelativeTo(null);
        }

        // ===== GERAR GALINHAS =====
        private void gerarGalinhas() {
            listaGalinhas.clear();
            Random r = new Random();

            for (int i = 0; i < 5; i++) {
                listaGalinhas.add(new Galinha(r.nextInt(300), r.nextInt(350) + 50));
            }
        }

        // ===== GERAR EQUAÇÃO (RESULTADO 1 a 5) =====
        private void gerarNovaEquacao() {
            Random r = new Random();

            int a, b, tipo;
            int resultado = 0;
            String eq = "";

            while (true) {
                tipo = r.nextInt(2);  // 0 = soma, 1 = subtração
                a = r.nextInt(5) + 1; // 1 a 5
                b = r.nextInt(5) + 1; // 1 a 5

                if (tipo == 0) { // SOMA
                    resultado = a + b;
                    if (resultado >= 1 && resultado <= 5) {
                        eq = a + " + " + b + " = ?";
                        break;
                    }
                } else { // SUBTRAÇÃO
                    resultado = a - b;
                    if (resultado >= 1 && resultado <= 5) {
                        eq = a + " - " + b + " = ?";
                        break;
                    }
                }
            }

            objetivo = resultado;
            salvasNestaRodada = 0;
            equacaoAtual = eq;
        }

        // ===== SALVAR PONTUAÇÃO =====
        @Override
        public void salvarPontuacao() {
            try {
                FileWriter fw = new FileWriter("pontuacao_galinheiro.txt");
                fw.write("Total de galinhas salvas: " + pontuacao);
                fw.close();
                JOptionPane.showMessageDialog(this, "Pontuação salva!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar.");
            }
        }
    }

    // ===== MAIN =====
    public static void main(String[] args) {
        new JogoGalinheiro();
    }
}
