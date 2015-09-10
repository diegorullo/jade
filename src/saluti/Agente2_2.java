package saluti;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Agente2_2 extends Agent {

    protected void setup() {

        System.out.println("Buongiorno, mi presento sono " + getName() + 
            " e sono pronto ad operare!");

        addBehaviour(new Behaviour() {

            private int step = 0;
 
            public void action() {

                switch(step) {
                    case 0:
                        MessageTemplate mt0 = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
                        ACLMessage msg0 = receive(mt0);
                        if (msg0 != null) {
                            if (msg0.getContent().equals("Buongiorno, come sta?")) {
                                System.out.println(getName() + ": rivevuta QUERY_IF");
                                ACLMessage reply0 = msg0.createReply();
                                reply0.setPerformative(ACLMessage.INFORM_IF);
                                reply0.setContent("Bene, grazie. E lei?");
                                send(reply0);
                                System.out.println(getName() + ": inviata INFORM_IF");
                                step++;
                            }
                        } else {
                            block();
                        }
                        break;
                    case 1:
                        MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                        ACLMessage msg1 = receive(mt1);
                        if (msg1 != null) {
                            if (msg1.getContent().equals("Bene, grazie.")) {
                                System.out.println(getName() + ": rivevuta INFORM");
                                step++;
                            }
                        } else {
                            block();
                        }
                        break;
                   }

                }

            public boolean done() {
                return step == 2;
            }

        });

    }

}
                                

