/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package taskManagement;

import jade.core.Agent;
/**
 *
 * @author sp127567
 */



import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Initiator extends Agent {
    //il router da raggiungere
    private String targetRouter;
    //la lista dei Partecipant
    private AID[] partecipants = {new AID("partecipant1", AID.ISLOCALNAME),
                                  new AID("partecipant2", AID.ISLOCALNAME)};
    protected void setup() {

        System.out.println("[initiator]Buongiorno, mi presento sono " + getName() + 
            " e sono pronto ad ASSEGNARE task!");
        
        
        addBehaviour(new Behaviour() {
            private AID bestHopAgent; //agente che fornisce le condizioni migliori (minori hop)
            private int bestHop;  //il migliore (minore) numero di hop
            private int repliesCnt = 0; // contatore di repliche ricevute dai Partecipant
            private MessageTemplate mt; // template per ricevere repliche                
            private int step = 0; 

            public void action() {
                switch(step) {
                    case 0:   
                        ACLMessage msgCFP = new ACLMessage(ACLMessage.QUERY_IF);
                        msgCFP.addReceiver(new AID("Partecipant1", AID.ISLOCALNAME));
                        msgCFP.addReceiver(new AID("Partecipant2", AID.ISLOCALNAME));
                        msgCFP.setLanguage("Italian");
                        msgCFP.setContent("CFP");
                        send(msgCFP);
                        step++;
                        break;
                        //
                    case 1:
                        ACLMessage reply= myAgent.receive(mt);
                        if(reply != null) {
                        // Reply ricevuto
                            if(reply.getPerformative() == ACLMessage.PROPOSE) {
                        // Ho ricevuto una proposta...
                                int price = Integer.parseInt(reply.getContent());
                                if(bestHopAgent == null|| price < bestHop) {
                        // Migliori condizioni di servizio(numero minimo di hop)
                                    bestHop = price;
                                    bestHopAgent = reply.getSender();
                                }
                            }
                            repliesCnt++;
                            if(repliesCnt >= partecipants.length) {
                        // Tutte le repliche sono state ricevute
                                step = 2; 
                            }   
                        }
                        else {
                            block();
                        }
                        break;
                            
                        
               
                    case 2:
                        // manda l'ordine di inoltro al router che raggiunge il target in meno hop
                        ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                        order.addReceiver(bestHopAgent);
                        order.setContent(targetRouter);
                        order.setConversationId("instradamento");
                        order.setReplyWith("order"+System.currentTimeMillis());
                        myAgent.send(order);
                        // preparazione del template per recuperare l'ordine d'acquisto
                        mt = MessageTemplate.and(MessageTemplate.MatchConversationId("instradamento"),
                                                MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                        step = 3;
                        break;
                    case 3:
                        // Receive the purchase order reply 
                        reply = myAgent.receive(mt);
                        if(reply != null) {
                        // Purchase order reply received
                            if(reply.getPerformative() == ACLMessage.INFORM) {
                        // Purchase successful. We can terminate
                                System.out.println(targetRouter+"instradato con successo.");
                                System.out.println("Price = "+bestHop);
                                myAgent.doDelete();
                            }
                            step = 4;
                        }
                        else {
                            block();
                        }
                        break;               
                        
                }
                     

            }

            public boolean done() {
                return ((step == 2 && bestHopAgent == null) || step == 4);
            }
            
        });
    }

}
