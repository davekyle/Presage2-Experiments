/**
 * 
 */
package uk.ac.imperial.dws04.Presage2Experiments.IPCon.Messages;

import org.drools.runtime.StatefulKnowledgeSession;

import com.google.inject.Inject;

import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.rules.MessagesToRuleEngine;

/**
 * @author dws04
 *
 */
public class IPConMsgToRuleEngine extends MessagesToRuleEngine {

	private final StatefulKnowledgeSession sessionPtr;

	@Inject
	public IPConMsgToRuleEngine(StatefulKnowledgeSession session) {
		super(session);
		sessionPtr = session;
	}
	
	/**
	 * Inserts the IPConAction to the kbase if the message is an instanceof IPConActionMsg,
	 * otherwise inserts the message to the kbase.
	 * 
	 * @param m Message to be processed
	 * @return Message the message that was processed
	 * @see uk.ac.imperial.presage2.rules.MessagesToRuleEngine#constrainMessage(uk.ac.imperial.presage2.core.network.Message)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Message constrainMessage(Message m){
		if (m instanceof IPConActionMsg) {
			sessionPtr.insert(((IPConActionMsg)m).getData());
			return m;
		}
		else return super.constrainMessage(m);
	}

}
