package de.gdiservice.bplan.poi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FPlanGroup {
    
    public final static Pattern p3stellig = Pattern.compile("(\\d+)_(\\d+)_(\\d+)\\.");
    public final static Pattern p2stellig = Pattern.compile("(\\d+)_(\\d+)\\.");
    
   
    
    /**
     * Zerlegt der FPlan in Versionen durch Auswertung des Rreferenznamens.
     * z.B. "amt_rostocker_heide_moenchhagen_fplan_3_1_1_1.pdf",
     * Die 3. Stelle der Nummerierung wird als Version angenommen  
     * 
     * @param orgPlan
     * @return
     */
    public static List<FPlan> split(FPlan orgPlan) {
        
        List<FPlanIndexed> plans = new ArrayList<>();
        List<FPlan> result;
        
        PGSpezExterneReferenz[] pgExterneReferenzs = orgPlan.getExternereferenzes();
        
        int lastVersion = -1;
        FPlanIndexed nPlan = null;
        if (pgExterneReferenzs!=null && pgExterneReferenzs.length>0) {
            for (int i=0; i<pgExterneReferenzs.length; i++) {
                SpezExterneRef er = pgExterneReferenzs[i].getExterneRef();
                String refName = er.referenzname;
                if (refName == null) {
                    throw new IllegalArgumentException("Fehler beim Aufteilen des FPlans, ExterneReferenz ohne ReferenzName ["+orgPlan.getGml_id()+" "+orgPlan.getName()+"]");
                }
                
                Matcher m = p3stellig.matcher(refName);
                int version = -1;
                if (m.find() && m.groupCount()==3) {
                    version = Integer.parseInt(m.group(2));
                } else  {
                    Matcher m2 = p2stellig.matcher(refName);
                    if (m2.find() && m2.groupCount()==2) {
                        version = Integer.parseInt(m2.group(2));
                    }
                }
                if (version<0) {
                    throw new IllegalArgumentException("Fehler beim Aufteilen des FPlans, ReferenzName entspricht nicht dem Muster ["+orgPlan.getGml_id()+" "+orgPlan.getName()+"]");
                }
                    // System.out.println(orgPlan.getGml_id()+"  "+refName+"  "+idx+" "+version);
                if (lastVersion!=version) {
                    lastVersion = version;
                    nPlan = new FPlanIndexed(clone(orgPlan), lastVersion);
                    if (version>0) {
                        nPlan.plan.name = orgPlan.name + " " + String.valueOf(version) + ". Änderung";
                    }
                    plans.add(nPlan);
                }
                nPlan.plan.addExterneReferenz(pgExterneReferenzs[i]);
                
            }
        
            Collections.sort(plans, new Comparator<FPlanIndexed>() {
                @Override
                public int compare(FPlanIndexed o1, FPlanIndexed o2) {
                    return Integer.compare(o1.idx, o2.idx);
                };
            });
            result = new ArrayList<>(plans.size());
            for (int i=0; i<plans.size(); i++) {
                result.add(plans.get(i).plan);
            }
        } else {
            result = Collections.singletonList(orgPlan);
        }
        return result;
        
    }
    
    static class FPlanIndexed {
        FPlan plan;
        int idx;
        
        public FPlanIndexed(FPlan plan, int idx) {
            this.plan = plan;
            this.idx = idx;
        }
        
    }
    

    static FPlan clone(FPlan orgPlan) {
        FPlan plan = new FPlan();
        plan.name = orgPlan.name;
        plan.gml_id = orgPlan.gml_id;
        plan.nummer = orgPlan.nummer;
        plan.planart = orgPlan.planart;
        plan.rechtsstand = orgPlan.rechtsstand;
        plan.verfahren = orgPlan.verfahren;
        plan.wirksamkeitsdatum = orgPlan.wirksamkeitsdatum;
        plan.auslegungsstartdatum = orgPlan.auslegungsstartdatum;
        plan.auslegungsenddatum = orgPlan.auslegungsenddatum;
        plan.gemeinde = orgPlan.gemeinde;        
        plan.geom = orgPlan.geom; 
        
        return plan;
    }
    

}
