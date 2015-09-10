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
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Partecipant2 extends Agent {
    private Hashtable catalogue;
        
    protected void setup() {
        catalogue = new Hashtable();
        catalogue.put("unito", new Integer(4));
        catalogue.put("polito", new Integer(2));
        catalogue.put("cselt", new Integer(1));
        catalogue.put("unimi", new Integer(1));
        //catalogue.put("unina", new Integer(5));
        
        Enumeration keys = catalogue.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = catalogue.get(key);
            System.out.println("[partecipant1] conosco <"+key+">, raggiungibile in <"+value+"> hop.");
        }
	
        
        
        System.out.println("Buongiorno, mi presento sono " + getName() + 
            " e sono pronto ad ESEGUIRE task! (instradamento messaggi)");

        addBehaviour(new OfferRequestsServer());
        addBehaviour(new PurchaseOrdersServer());
    }
    
    protected void takeDown(){
        System.out.println("[partecipant1]-agent "+getAID().getName()+" sto terminando...");    
    }

    private class OfferRequestsServer extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null){
                //messaggio ricevuto: va processato...
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();
                Integer price = (Integer) catalogue.get(title);
                if (price!=null){
                    // il router è raggiungibile...
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));                    
                }
                else {
                    // il router non è tra quelli raggiungibili...
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("non-raggiungibile");
                }
                myAgent.send(reply);    
            }
        }
        
    }
    
    private class PurchaseOrdersServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // ACCEPT_PROPOSAL messaggio ricevuto, lo processo...
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();

                //FIXME: nel nostro caso non ha senso rimuovere da catalogo...
                //avrebbe senso se aggiornassimo la tabella di routing
                //(router irraggiungibile)
                //poi però dovremmo gestire la notifica se
                //nuovamente raggiungibile...
                Integer price = (Integer) catalogue.get(title);
                
                if (price != null) {
                        reply.setPerformative(ACLMessage.INFORM);
                        System.out.println(title+" spedito messaggio per conto di "+msg.getSender().getName());
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