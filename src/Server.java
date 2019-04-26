
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import java.net.*;
import java.io.*;
import java.util.Collections;
import java.util.Comparator;


/**
     *aplikacja serwerowa wysyłająca dane o poziomach, najwyższych wynikach
     */
    public class Server extends JFrame {



        private Float xPosBall;
        private float yPosBall;
        private float xPosFinish;
        private float yPosFinish;
        private int obstacleNumber;
        private float xPosSuperPoint;
        private float yPosSuperPoint;
        private JPanel panel;
        private JLabel statement;


        private ArrayList<Float> obstX, obstY, obstHeight, obstWidth;

        private ArrayList<PlayersScore> scoresList;

        /**
         * klasa opisująca wyniki graczy
         */
        private class PlayersScore {
            private int deaths;
            private float time;
            private String name;

            /**
             * kontruktor przyjmujący informacje o graczu, który właśnie zakończył grę
             *
             * @param death ilość żyć, które pozostały graczowi
             * @param timer czas, w który gracz przeszedł grę
             * @param namie nazwa gracza
             */
            public PlayersScore(int death, float timer, String namie) {
                deaths = death;
                time = timer;
                name = namie;
            }

            /**
             * informacja o ilość żyć, które pozostały graczowi
             *
             * @return
             */
            public int getDeaths() {

                return (deaths);
            }

            /**
             * informacja o czasie, który był potrzebny, by przejść grę
             *
             * @return
             */
            public float getTime() {

                return (time);
            }

            /**
             * informacja o nazwie gracza
             */
            public String getName() {

                return (name);
            }
        }

        private ServerSocket serversocket;

        /**
         * tworzenie okna serwera
         */
        private void launchFrame() {


            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setVisible(true);
            setTitle("Ball: The Game | Server");
            statement = new JLabel("The server is up! Waiting for clients...");
            System.out.println("The server is up! Waiting for clients...");
            statement.setFont(new Font("Comic Sans MS", Font.PLAIN, 20));
            statement.setForeground(Color.WHITE);
            statement.setAlignmentX(CENTER_ALIGNMENT);
            statement.setAlignmentY(CENTER_ALIGNMENT);
            ImageIcon icon = new ImageIcon("images/icon.png");
            statement.setIcon(icon);


            panel = new JPanel() {
                public void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    BufferedImage background = null;

                    try {

                        background = ImageIO.read(this.getClass().getResource("images/serverBackground.jpg"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
                }
            };
            panel.setBackground(Color.black);
            panel.add(statement);
            add(panel);
        }


        /**
         * ropoczęcie działania aplikacji serwerowej
         *
         * @param args
         */
        public static void main(String[] args) {

            Server server = new Server();
          //  server.launchFrame();
            server.setSize(500, 200);
            String port;
            int portNumb;
            InetAddress IP;
            String host;

            try {
                port = args[0];

            }catch(Exception e){

                System.err.print("A problem with port number has occurred. Is it inserted properly?");
                port="997";
            }

            try {
                IP = InetAddress.getLocalHost();
                host = IP.getHostName();
                portNumb = Integer.parseInt(port);
                server.serversocket = new ServerSocket(portNumb);
                FileWriter fileWriter = new FileWriter("./resources/server.txt");
                System.out.println("Host name: " + host);
                System.out.println("Port number: " + port);
                System.out.println("The server is up! Waiting for clients...");

                fileWriter.write(host);
                fileWriter.write(" ");
                fileWriter.write(port);

                fileWriter.close();




            } catch (Exception e) {
                e.printStackTrace();
            }

            while (true) {

                try {
                    Socket socket = server.serversocket.accept();


                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    DataOutputStream datastream = new DataOutputStream(socket.getOutputStream());

                    int choice = inputStream.readInt();
                    switch (choice) {


                        case 1:
                            try {
                                int income = inputStream.readInt();
                                server.getLevel(income);

                                datastream.writeFloat(server.xPosBall);
                                datastream.writeFloat(server.yPosBall);
                                datastream.writeFloat(server.xPosFinish);
                                datastream.writeFloat(server.yPosFinish);
                                datastream.writeFloat(server.xPosSuperPoint);
                                datastream.writeFloat(server.yPosSuperPoint);
                                datastream.writeInt(server.obstacleNumber);

                                for (int i = 0; i < server.obstacleNumber; i++) {

                                    datastream.writeFloat(server.obstX.get(i));
                                    datastream.writeFloat(server.obstY.get(i));
                                    datastream.writeFloat(server.obstHeight.get(i));
                                    datastream.writeFloat(server.obstWidth.get(i));
                                }
                               // server.statement.setText("Data for level nr " + income + " was succesfully sent!");
                                System.out.println("Data for level nr " + income + " was succesfully sent!");

                                break;
                            } catch (Exception e) {
                                e.printStackTrace();
                               // server.statement.setText("Unfortunetely...something went wrong...");
                                System.out.println("Unfortunetely...something went wrong...");
                            }

                        case 2:
                            server.getHighscore();

                            for (int i = 0; i < 10; i++) {
                                datastream.writeFloat(server.scoresList.get(i).getTime());
                                datastream.writeInt(server.scoresList.get(i).getDeaths());
                                byte[] nameData = server.scoresList.get(i).getName().getBytes("UTF-8");
                                datastream.writeInt(nameData.length);
                                datastream.write(nameData);

                            }
                           // server.statement.setText("The Highscores were succesfully sent!");
                            System.out.println("The Highscores were succesfully sent!");
                            break;
                        case 3:
                            int incomeLives = inputStream.readInt();
                            float incomeTime = inputStream.readFloat();
                            int nameDataLength = inputStream.readInt();
                            byte[] nameData = new byte[nameDataLength];
                            inputStream.readFully(nameData);
                            String incomeName = new String(nameData, "UTF-8");
                            server.updateHighScore(incomeName, incomeLives, incomeTime, server.scoresList);
                           // server.statement.setText("New Highscores were succesfully sent!");
                            System.out.println("New Highscores were succesfully sent!");
                            break;

                        default:
                         //   server.statement.setText("Unfortunetely...something went wrong...");
                            System.out.println("Unfortunetely...something went wrong...");
                            break;

                    }


                    socket.close();
                } catch (Exception e) {
                 //   server.statement.setText("Unfortunetely...something went wrong...");
                    System.out.println("Unfortunetely...something went wrong...");
                    e.printStackTrace();

                }
            }
        }


        /**
         * pobranie danych o pewnym poziomie
         *
         * @param i
         */
        private void getLevel(int i) {

            String fileName = "./resources/levels/lvl" + i;
            fileName = fileName + ".xml";

            obstX = new ArrayList<>();
            obstY = new ArrayList<>();
            obstWidth = new ArrayList<>();
            obstHeight = new ArrayList<>();
            try {
                File lvlSetFile = new File(fileName);
                DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(lvlSetFile);
                doc.getDocumentElement().normalize();

                NodeList ballList = doc.getElementsByTagName("Ball");
                Node ballNode = ballList.item(0);

                if (ballNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) ballNode;

                    xPosBall = Float.parseFloat(element.getElementsByTagName("xPos").item(0).getTextContent());
                    yPosBall = Float.parseFloat(element.getElementsByTagName("yPos").item(0).getTextContent());
                }

                NodeList obstNumber = doc.getElementsByTagName("ObstaclesNumber");
                Node obstNumberNode = obstNumber.item(0);
                if (obstNumberNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) obstNumberNode;

                    obstacleNumber = Integer.parseInt(element.getElementsByTagName("number").item(0).getTextContent());
                }


                NodeList finList = doc.getElementsByTagName("Finish");
                Node finishNode = finList.item(0);
                if (finishNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) finishNode;

                    xPosFinish = Float.parseFloat(element.getElementsByTagName("xPos").item(0).getTextContent());
                    yPosFinish = Float.parseFloat(element.getElementsByTagName("yPos").item(0).getTextContent());

                }

                NodeList superList = doc.getElementsByTagName("SuperPoint");
                Node superNode = superList.item(0);
                if (superNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) superNode;

                    xPosSuperPoint = Float.parseFloat(element.getElementsByTagName("xPos").item(0).getTextContent());
                    yPosSuperPoint = Float.parseFloat(element.getElementsByTagName("yPos").item(0).getTextContent());
                }


                NodeList nodeList = doc.getElementsByTagName("Obstacle");

                for (int p = 0; p < nodeList.getLength(); p++) {
                    Node tmpNode = nodeList.item(p);

                    if (tmpNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) tmpNode;

                        float xPos = Float.parseFloat(element.getElementsByTagName("xPos").item(0).getTextContent());
                        float yPos = Float.parseFloat(element.getElementsByTagName("yPos").item(0).getTextContent());
                        float height = Float.parseFloat(element.getElementsByTagName("hight").item(0).getTextContent());
                        float width = Float.parseFloat(element.getElementsByTagName("weidth").item(0).getTextContent());
                        obstX.add(xPos);

                        obstY.add(yPos);
                        obstHeight.add(height);
                        obstWidth.add(width);
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            ;

        }

        /**
         * pobranie najwyższych wyników
         */
        private void getHighscore() {
            try {
                this.scoresList = new ArrayList<>();
                File xmlSetFile = new File("./resources/highscores.xml");
                DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(xmlSetFile);
                doc.getDocumentElement().normalize();
                NodeList nodeList = doc.getElementsByTagName("Player");

                for (int p = 0; p < nodeList.getLength(); p++) {
                    Node tmpNode = nodeList.item(p);


                    if (tmpNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) tmpNode;

                        String Name = (element.getElementsByTagName("name").item(0).getTextContent());
                        int Deaths = Integer.parseInt(element.getElementsByTagName("lives").item(0).getTextContent());
                        float Time = Float.parseFloat(element.getElementsByTagName("time").item(0).getTextContent());

                        scoresList.add(new PlayersScore(Deaths, Time, Name));
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        /**
         * zapis najwyższych wyników
         *
         * @param highscoreList
         */
        private void saveNewHighscores(ArrayList<PlayersScore> highscoreList) {
            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.newDocument();
                Element rootElement = doc.createElement("highscores");
                doc.appendChild(rootElement);
                for (int i = 0; i < 10; i++) {

                    Element Player = doc.createElement("Player");
                    rootElement.appendChild(Player);
                    Element name = doc.createElement("name");
                    name.appendChild(doc.createTextNode(highscoreList.get(i).getName()));
                    Player.appendChild(name);
                    Element deaths = doc.createElement("lives");
                    deaths.appendChild(doc.createTextNode(Integer.toString(highscoreList.get(i).getDeaths())));
                    Player.appendChild(deaths);
                    Element time = doc.createElement("time");
                    time.appendChild(doc.createTextNode(Float.toString(highscoreList.get(i).getTime())));
                    Player.appendChild(time);

                }



                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");


                tr.transform(new DOMSource(doc),
                        new StreamResult(new FileOutputStream("./resources/highscores.xml")));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        /**
         * aktualizacja najwyższych wyników
         *
         * @param name       nazwa gracza
         * @param deaths     ilość pozostałych żyć
         * @param time       czas, w którym gracz przeszedł grę
         * @param scoresList tablica z posortowanymi wynikami
         */
        private void updateHighScore(String name, int deaths, float time, ArrayList<PlayersScore> scoresList) {


            this.scoresList.add(new PlayersScore(deaths, time, name));
            sort(this.scoresList);
            saveNewHighscores(this.scoresList);



        }

        /**
         * sortowanie wyników
         *
         * @param highscoreList
         */
        private static void sort(ArrayList<PlayersScore> highscoreList) {
            Collections.sort(highscoreList, new Comparator<PlayersScore>() {
                @Override
                public int compare(PlayersScore player1, PlayersScore player2) {
                    if (player1.getDeaths() < player2.getDeaths()) {
                        return 1;
                    }
                    if (player1.getDeaths() > player2.getDeaths()) {
                        return -1;
                    } else {
                        if (player1.getTime() > player2.getTime())
                            return 1;
                        if (player1.getTime() < player2.getTime())
                            return -1;
                        else
                            return 0;
                    }
                }
            });

        }
    }



