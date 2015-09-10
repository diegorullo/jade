package saluti;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Agente1_2 extends Agent {

    protected void setup() {

        System.out.println("Buongiorno, mi presento sono " + getName() + 
            " e sono pronto ad operare!");

        addBehaviour(new Behaviour() {

            private int step = 0;
 
            public void action() {

                switch(step) {
                    case 0:
                        ACLMessage msg0 = new ACLMessage(ACLMessage.QUERY_IF);
                        msg0.addReceiver(new AID("AgenteDueDue", AID.ISLOCALNAME));
                        msg0.setLanguage("Italian");
                        msg0.setContent("Buongiorno, come sta?");
                        send(msg0);
                        System.out.println(getName() + ": inviata QUERY_IF");
                        step++;
                        break;
                    case 1:
                        MessageTemplate mt0 = MessageTemplate.MatchPerformative(ACLMessage.INFORM_IF);
                        ACLMessage reply0 = receive(mt0);
                        if (reply0 != null) {
                            if (reply0.getContent().equals("Bene, grazie. E lei?")) {
                                System.out.println(getName() + ": rivevuta INFORM_IF");
                                ACLMessage msg1 = reply0.createReply();
                                msg1.setPerformative(ACLMessage.INFORM);
                                msg1.setContent("Bene, grazie.");
                                send(msg1);
                                System.out.println(getName() + ": inviata INFORM");
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
                                

