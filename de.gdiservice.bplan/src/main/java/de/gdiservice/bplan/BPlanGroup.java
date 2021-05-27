package de.gdiservice.bplan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BPlanGroup {
    
    final static Pattern p = Pattern.compile("(\\d+)_(\\d+)_(\\d+)\\.");
    
   
    
    /**
     * Zerlegt der BPlan in Versionen durch Auswertung des Rreferenznamens.
     * z.B. "amt_rostocker_heide_moenchhagen_bplan_3_1_1_1.pdf",
     * Die 3. Stelle der Nummerierung wird als Version angenommen  
     * 
     * @param orgPlan
     * @return
     */
    public static List<BPlan> split(BPlan orgPlan) {
        
        List<BPlanIndexed> plans = new ArrayList<>();       
        
        PGExterneReferenz[] pgExterneReferenzs = orgPlan.getExternereferenzes();
        
        int idx = -1;
        BPlanIndexed nPlan = null;
        
        for (int i=0; i<pgExterneReferenzs.length; i++) {
            ExterneRef er = pgExterneReferenzs[i].getExterneRef();
            String refName = er.referenzname;
            
            
            Matcher m = p.matcher(refName);
            if (m.find() && m.groupCount()==3) {
                int version = Integer.parseInt(m.group(2));
                // System.out.println(orgPlan.getGml_id()+"  "+refName+"  "+idx+" "+version);
                if (idx!=version) {
                    idx = version;
                    nPlan = new BPlanIndexed(clone(orgPlan), idx);
                    plans.add(nPlan);
                }
                nPlan.plan.addExterneReferenz(pgExterneReferenzs[i]);
                
            }
            else {
                throw new IllegalArgumentException("Fehler beim Aufteilen des BPlans ["+orgPlan.getGml_id()+" "+orgPlan.getName()+"]");
            }
        }
        
        Collections.sort(plans, new Comparator<BPlanIndexed>() {
            @Override
            public int compare(BPlanIndexed o1, BPlanIndexed o2) {
                return Integer.compare(o1.idx, o2.idx);
            };
        });
        ArrayList<BPlan> result = new ArrayList<>(plans.size());
        for (int i=0; i<plans.size(); i++) {
            result.add(plans.get(i).plan);
        }
        return result;
        
    }
    
    static class BPlanIndexed {
        BPlan plan;
        int idx;
        
        public BPlanIndexed(BPlan plan, int idx) {
            this.plan = plan;
            this.idx = idx;
        }
        
    }
    

    static BPlan clone(BPlan orgPlan) {
        BPlan plan = new BPlan();
        plan.id = orgPlan.id;
        plan.name = orgPlan.name;
        plan.gml_id = orgPlan.gml_id;
        plan.nummer = orgPlan.nummer;
        plan.planart = orgPlan.planart;
        plan.rechtsstand = orgPlan.rechtsstand;
        plan.inkrafttretensdatum = orgPlan.inkrafttretensdatum;
        plan.gemeinde = orgPlan.gemeinde;        
        plan.geom = orgPlan.geom; 
        
        return plan;
    }
    

}
