/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package taskManagement;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Partecipant2 extends Agent {
    private Hashtable routingTableHops;
    private Hashtable routingTableThrough;
    private String nodoDest;
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
            System.out.println("[partecipant2] conosco <"+routeTBL[j].getName()+">, raggiungibile in <"+routeTBL[j].getHops()+"> hop - da interfaccia <"+routeTBL[j].getThrough()+">");     
        }*/
        
        routingTableHops = new Hashtable();
        routingTableHops.put("unito", new Integer(2));
        routingTableHops.put("polito", new Integer(2));
        routingTableHops.put("cselt", new Integer(1));
        routingTableHops.put("unimi", new Integer(1));
        
        routingTableThrough = new Hashtable();
        routingTableThrough.put("unito", "Partecipant1");
        routingTableThrough.put("polito", "Partecipant1");
        routingTableThrough.put("cselt", "direct");
        routingTableThrough.put("unimi", "direct");
        //catalogue.put("unina", new Integer(5));
        
        Enumeration keys = routingTableHops.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = routingTableHops.get(key);
            System.out.println("[partecipant2] conosco <"+key+">, raggiungibile in <"+value+"> hop - da interfaccia <"+routingTableThrough.get(key)+">");     
        }
        
        System.out.println("[partecipant2] Buongiorno, mi presento sono " + getName() + 
            " e sono pronto ad ESEGUIRE task! (instradamento messaggi)");

        addBehaviour(new OffreServizio());
        addBehaviour(new EsegueServizio());
    }
    
    protected void takeDown(){
        System.out.println("[partecipant2]-agent "+getAID().getName()+" sto terminando...");    
    }

    private class OffreServizio extends CyclicBehaviour {
        private MessageTemplate mt=
                MessageTemplate.MatchPerformative(ACLMessage.CFP);
        
        
        public void action() {

           ACLMessage msg = myAgent.receive(mt);
            if (msg != null){
                System.out.println("[partecipant2]-agent "+getAID().getName()+" mess arrivato (CFP)");   
                //CFP ricevuta: va processata...
                nodoDest = msg.getContent();
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
                System.out.println("[partecipant2] Agent "+getAID().getName()+" inviata PROPOSE");   
            }
        }
        
    }
    
    private class EsegueServizio extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // ACCEPT_PROPOSAL messaggio ricevuto, lo processo...
                try {
                    String path = "C:/" +nodoDest+"/"+msg.getContent();
                    System.out.println("[partecipant2] Tento di scrivere in: "+path);
                    File file = new File(path);
                    ACLMessage reply = msg.createReply();
                    if (file.createNewFile()){
                        System.out.println("[partecipant2] Il messaggio è stato recapitato");
                
                        
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(String.valueOf("MESSAGGIO-RECAPITATO")); 
                    }
                    else {
                            System.out.println("[partecipant2] Errore disco. messaggio NON recapitato");
                            
                            reply.setPerformative(ACLMessage.FAILURE);
                            reply.setContent(String.valueOf("MESSAGGIO-NON-RECAPITATO"));
                    }
                    myAgent.send(reply);
                
                }
                catch (IOException e) {
                        e.printStackTrace();
                }
               
                
            }
            else {
                block();
            }
        }
    }  // End of inner class OfferRequestsServer
    

}