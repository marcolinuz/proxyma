package org.homelinux.nx01.proxyma.core;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * User: makko
 * Date: 27-Apr-2007
 * Time: 13.54.14
 * </p><p>
 * This class is a Singleton that stores an hashtable of ProxymaRulkeSet.
 * Every record in the hashtable is referred to a single instance of Proxyma and
 * countains the conffigurations for all the proxed resources for that instance.
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class RuleSetsPool {
    /**
     * Constructor for this class
     */
    protected RuleSetsPool() {
        this.proxymaRuleSets = new ConcurrentHashMap();
    }

    /**
     * Static method to call to obtain the singleton class that manages the rulesets
     * for all the proxyma instances in this virtual machine.
     *
     * @return the only instance of the global RuleSetsPool
     */
    public static synchronized RuleSetsPool getInstance() {
        if (instance == null) {
            instance = new RuleSetsPool();
        }
        return instance;
    }

    /**
     * Obtain a set of configuration rules for the passed applicationContext
     *
     * @param applicationContext a unique string that identifies the instance of proxyma (I alwais use the application context path)
     * @return a ConcurrentHashMap that countains the ruleset for the wanted instance.
     */
    public RuleSet getRuleSet(String applicationContext) {
        RuleSet retValue = null;
        if (proxymaRuleSets.containsKey(applicationContext)) {
            retValue = (RuleSet) proxymaRuleSets.get(applicationContext);
        } else {
            retValue = new RuleSet();
            proxymaRuleSets.put(applicationContext, retValue);
        }

        return retValue;
    }

    private static RuleSetsPool instance = null;
    private ConcurrentHashMap proxymaRuleSets = null;
}
