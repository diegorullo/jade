/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package taskManagement;

import jade.core.Agent;
import jade.core.behaviours.*;
import java.util.*;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Partecipant1 extends Agent {
    private Hashtable routingTableHops;
    private Hashtable routingTableThrough;
    private AID nodoDest;
    protected class MyNode {
        private String name;
        private int hops;
        private String reachableThrough;

        private MyNode(String pName, int pHops, String pThrough){
            this.name = pName;
            this.hops = pHops;
            this.reachableThrough = pThrough;
        }

        public void setName(String pName) { 
            this.name=pName;
        }
        public void setHops(int pHops) { 
            this.hops=pHops;
        }
        public void setThrough(String pThrough) { 
            this.name=pThrough;
        }
        public String getName() { 
            return this.name;
        }
        public int getHops() { 
            return this.hops;
        }
        public String getThrough() { 
            return this.reachableThrough;
        }
    }   
    protected void setup() {
              // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("instradatore");
        sd.setName("JADE-instradatore");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        
        /*
        MyNode[] routeTBL;	
        routeTBL = new MyNode[3];
        routeTBL[0] = new MyNode("cselt",1,"direct");
        routeTBL[1] = new MyNode("unito",1,"direct");
        routeTBL[2] = new MyNode("cselt",1,"direct");
        
        int j;
        for (j=0; j<3; j++){
            System.out.println("[partecipant1] conosco <"+routeTBL[j].getName()+">, raggiungibile in <"+routeTBL[j].getHops()+"> hop - da interfaccia <"+routeTBL[j].getThrough()+">");     
        }*/
        
        routingTableHops = new Hashtable();
        routingTableHops.put("unito", new Integer(1));
        routingTableHops.put("polito", new Integer(1));
        routingTableHops.put("cselt", new Integer(1));
        
        routingTableThrough = new Hashtable();
        routingTableThrough.put("unito", "direct");
        routingTableThrough.put("polito", "direct");
        routingTableThrough.put("cselt", "direct");

        //catalogue.put("unina", new Integer(5));
        
        Enumeration keys = routingTableHops.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = routingTableHops.get(key);
            System.out.println("[partecipant1] conosco <"+key+">, raggiungibile in <"+value+"> hop - da interfaccia <"+routingTableThrough.get(key)+">");     
        }
        
        System.out.println("Buongiorno, mi presento sono " + getName() + 
            " e sono pronto ad ESEGUIRE task! (instradamento messaggi)");

        addBehaviour(new OffreServizio());
        addBehaviour(new EsegueServizio());
    }
    
    protected void takeDown(){
        System.out.println("[partecipant1]-agent "+getAID().getName()+" sto terminando...");    
    }

    private class OffreServizio extends CyclicBehaviour {
        private MessageTemplate mt=
                MessageTemplate.MatchPerformative(ACLMessage.CFP);
        
        
        public void action() {

           ACLMessage msg = myAgent.receive(mt);
            if (msg != null){
                System.out.println("[partecipant1]-agent "+getAID().getName()+" mess arrivato (CFP)");   
                //CFP ricevuta: va processata...
                String nodoDest = msg.getContent();
                ACLMessage reply = msg.createReply();
                Integer nHops = (Integer) routingTableHops.get(nodoDest);
                if (nHops!=null){
                    // il router è raggiungibile...
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(nHops.intValue()));                    
                }
                else {
                    
                    // il router non è tra quelli raggiungibili...
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("non-raggiungibile");
                }
                myAgent.send(reply);    
                System.out.println("[partecipant1]-agent "+getAID().getName()+" inviata PROPOSE");   
            }
        }
        
    }
    
    private class EsegueServizio extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // ACCEPT_PROPOSAL messaggio ricevuto, lo processo...
                
                ACLMessage proxyMsg = new ACLMessage(ACLMessage.INFORM);
                proxyMsg.addReceiver(nodoDest);
                proxyMsg.setContent(msg.getContent());
                proxyMsg.setConversationId("instradamento");
                proxyMsg.setReplyWith("route" + System.currentTimeMillis());
                myAgent.send(proxyMsg);
                
                String messageToForward = msg.getContent();
                
                
                ACLMessage reply = msg.createReply();

                //FIXME: in questo caso non ha senso rimuovere ...
                //avrebbe senso se aggiornassimo la tabella di routing
                //(router irraggiungibile)
                //poi però dovremmo gestire la notifica se
                //nuovamente raggiungibile...
                Integer nHops = (Integer) routingTableHops.get(messageToForward);
                
                if (nHops != null) {
                        reply.setPerformative(ACLMessage.INFORM);
                        System.out.println(messageToForward+" spedito messaggio per conto di "+msg.getSender().getName());
                }
                else {
                        // Il router non è raggiungibile dall' agente...
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setContent("non-raggiungibile");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }  // End of inner class OfferRequestsServer
    

}
                                


