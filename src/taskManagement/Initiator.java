/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taskManagement;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Initiator extends Agent {

    //il router da raggiungere
    private String targetRouter;
    private String targetMessage;

    //la lista dei Partecipant
    /*private AID[] partecipants = {new AID("partecipant1", AID.ISLOCALNAME),
     new AID("partecipant2", AID.ISLOCALNAME)};*/
    private AID[] partecipants;

    protected void setup() {

        System.out.println("[initiator]Buongiorno, mi presento sono " + getName()
                + " e sono pronto ad ASSEGNARE task!");
        System.out.println("[initiator]Argomenti:");
        Object[] args = getArguments();
        if (args != null && args.length == 2) {
            targetRouter = args[0].toString();
            targetMessage = args[1].toString();
            for (int i = 0; i < args.length; ++i) {
                System.out.println("- " + args[i]);
            }
            System.out.println("[initiator] tento di far consegnare <" + targetMessage + "> a <" + targetRouter);
            addBehaviour(new TickerBehaviour(this, 60000) {
                protected void onTick() {
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("instradatore");
                    template.addServices(sd);
                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        partecipants = new AID[result.length];
                        for (int i = 0; i < result.length; i++) {
                            partecipants[i] = result[i].getName();
                        }
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
                    myAgent.addBehaviour(new RequestPerformer());

                }
            });
        } else {
            System.out.println("[initiator] errore critico passaggio argomenti!");
            doDelete();
        }
        //FIXME: sviluppi futuri: controllare consistenza dei parametri passati
        //       e che siano effettivamente 2 altrimenti messaggio d'errore e uscita...

    }

    protected void takeDown() {
        System.out.println("[initiator] sto terminando...");
    }

    private class RequestPerformer extends Behaviour {

        private AID bestHopAgent; //agente che fornisce le condizioni migliori (minori hop)
        private int bestHop;  //il migliore (minore) numero di hop
        private int repliesCnt = 0; // contatore di repliche ricevute dai Partecipant
        private MessageTemplate mt; // template per ricevere repliche                
        private int step = 0;

        public void action() {
            switch (step) {
                case 0:
                    // manda la CFP a tutti i partecipant
                    // Send the cfp to all sellers
                    ACLMessage msgCFP = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < partecipants.length; ++i) {
                        msgCFP.addReceiver(partecipants[i]);
                    }
                    msgCFP.setLanguage("Italian");
                    msgCFP.setContent(targetRouter);
                    send(msgCFP);
                    System.out.println("[initiator] broadcast inviato");
                    
                    step = 1;
                    break;
                //
                case 1:
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Reply ricevuto
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // Ho ricevuto una proposta...
                            // devo valutare quella più conveniente, controllo il numero di hop...
                            int hops = Integer.parseInt(reply.getContent());
                            if (bestHopAgent == null || hops < bestHop) {
                                // Migliori condizioni di servizio(numero minimo di hop)
                                bestHop = hops;
                                bestHopAgent = reply.getSender();
                            }
                        }
                        repliesCnt++;
                        if (repliesCnt >= partecipants.length) {
                            System.out.println("[initiator] tutte le replice sono state ricevute");
                            // Tutte le repliche sono state ricevute
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;

                case 2:
                    // manda il messaggio da inoltrare al router che raggiunge il target in meno hop
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestHopAgent);
                    order.setContent(targetMessage);
                    order.setConversationId("instradamento");
                    order.setReplyWith("route" + System.currentTimeMillis());
                    myAgent.send(order);
                    
                    System.out.println("[initiator] proposta di: "+bestHopAgent+" ("+bestHop+" hops) accettata");
                    
                    // preparazione del template per recuperare l'ordine d'acquisto
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("instradamento"),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    
                    
                    // manda la REJECT_PROPOSAL a tutti i partecipant tranne il bestHopAgent
                    ACLMessage msgReject = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                    for (int i = 0; i < partecipants.length; ++i) {
                        if (partecipants[i].compareTo(bestHopAgent)!=0){
                            msgReject.addReceiver(partecipants[i]);
                        } else {
                        }
                    }
                    msgReject.setLanguage("Italian");
                    msgReject.setContent("instradamento");
                    send(msgReject);
                    System.out.println("[initiator] REJECT_PROPOSAL inviati");
                    
                    step = 3;
                    
                    
                    break;
                case 3:
                    // Riceve il messaggio di avvenuto instradamento...
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        // messaggio di avvenuto instradamento ricevuto...
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            // instradamento avvenuto con successo, il processo termina
                            System.out.println("[initiator] Messaggio verso <" + targetRouter + "> instradato con successo.");
                            System.out.println("[initiator] Num. hop = " + bestHop);
                            myAgent.doDelete();
                        }
                        step = 4;
                    } else {
                        block();
                    }
                    break;

            }

        }

        public boolean done() {
            return ((step == 2 && bestHopAgent == null) || step == 4);
        }

    }

}
