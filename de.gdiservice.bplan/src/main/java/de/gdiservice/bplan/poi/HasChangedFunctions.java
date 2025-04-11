package de.gdiservice.bplan.poi;

import java.util.Arrays;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HasChangedFunctions {
    
    final static Logger logger = LoggerFactory.getLogger(HasChangedFunctions.class);
    
    
    public static <T extends XPPlan>boolean hasChanged(T plan, T dbPlan) {
        if (plan instanceof BPlan && dbPlan instanceof BPlan) {
            return hasChanged((BPlan)plan, (BPlan)dbPlan);
        }
        if (plan instanceof FPlan && dbPlan instanceof FPlan) {
            return hasChanged((FPlan)plan, (FPlan)dbPlan);
        }
        if (plan instanceof SOPlan && dbPlan instanceof SOPlan) {
            return hasChanged((SOPlan)plan, (SOPlan)dbPlan);
        }
        throw new IllegalArgumentException("Parameter not both from the same Class or not Instances of BPlan|FPlan|SOPlan");
        
    }
    public static boolean hasChanged(SOPlan plan, SOPlan dbPlan) {



        if (HasChangedFunctions.hasChanged(plan.gemeinde, dbPlan.gemeinde)) {
            logger.info(String.format("<>gemeinde %s %s", toString(plan.gemeinde), toString(dbPlan.gemeinde)));
            return true;
        }
        if (HasChangedFunctions.hasChanged(plan.getExternereferenzes(), dbPlan.getExternereferenzes())) {
            logger.info(String.format("<>Externereferenzes %s %s", plan.getExternereferenzes(), dbPlan.getExternereferenzes()));
            return true;
        }

        if (plan.geom == null || dbPlan.geom == null) {
            throw new IllegalArgumentException("one Plan doesnt has a geometry");
        }
        if (!plan.geom.equals(dbPlan.geom)) {
            logger.info(String.format("<>geom %s %s", plan.geom, dbPlan.geom));
            return true;
        }
        if (hasChanged(plan.genehmigungsdatum, dbPlan.genehmigungsdatum)) {
            logger.info(String.format("<>inkrafttretensdatum %s %s", plan.genehmigungsdatum, dbPlan.genehmigungsdatum));
            return true;
        }
        if (hasChanged(plan.auslegungsstartdatum, dbPlan.auslegungsstartdatum)) {
            logger.info(String.format("<>auslegungsstartdatum %s %s", plan.auslegungsstartdatum, dbPlan.auslegungsstartdatum));
            return true;
        }
        if (hasChanged(plan.auslegungsenddatum, dbPlan.auslegungsenddatum)) {
            logger.info(String.format("<>auslegungsenddatum %s %s", plan.auslegungsenddatum, dbPlan.auslegungsenddatum));
            return true;
        }  
        if (hasChanged(plan.name, dbPlan.name)) {
            logger.info(String.format("<>name %s %s", plan.name, dbPlan.name));
            return true;
        }
        if (hasChanged(plan.nummer, dbPlan.nummer)) {
            logger.info(String.format("<>nummer %s %s", plan.nummer, dbPlan.nummer));
            return true;
        }

        if (plan.planart==null) {
            if (plan.planart!=null) {
                logger.info(String.format("<>planart %s %s", plan.planart, dbPlan.planart));
                return true;
            }
        } else if (!plan.planart.equals(dbPlan.planart)) {
            logger.info(String.format("<>planart %s %s", plan.planart, dbPlan.planart));
            return true;
        }
//        if (hasChanged(plan.rechtsstand, dbPlan.rechtsstand)) {
//            logger.info(String.format("<>rechtsstand %s %s", plan.rechtsstand, dbPlan.rechtsstand));
//            return true;
//        }
        
        if (hasChanged(plan.aendert, dbPlan.aendert)) {
            logger.info(String.format("<>aendert %s %s", plan.aendert, dbPlan.aendert));
            return true;
        }
        if (hasChanged(plan.wurdegeaendertvon, dbPlan.wurdegeaendertvon)) {
            logger.info(String.format("<>wurdegeaendertvon %s %s", plan.wurdegeaendertvon, dbPlan.wurdegeaendertvon));
            return true;
        }
//        if (hasChanged(plan.internalid, dbPlan.internalid)) {
//            logger.info(String.format("<>internalid %s %s", plan.internalid, dbPlan.internalid));
//            return true;
//        }
        
        
        if (hasChanged(plan.getUntergangsdatum(), dbPlan.getUntergangsdatum())) {
            logger.info(String.format("<>untergangsdatum %s %s", plan.untergangsdatum, dbPlan.untergangsdatum));
            return true;
        }
        
        if (hasChanged(plan.getTechnHerstellDatum(), dbPlan.getTechnHerstellDatum())) {
            logger.info(String.format("<>technHerstellDatum %s %s", plan.technHerstellDatum, dbPlan.technHerstellDatum));
            return true;
        }
        
        if (hasChanged(plan.planaufstellendegemeinde, dbPlan.planaufstellendegemeinde)) {
            logger.info(String.format("<>planaufstellendegemeinde %s %s", plan.planaufstellendegemeinde, dbPlan.planaufstellendegemeinde));
            return true;
        }
        return false;
    }    
    
    public static boolean hasChanged(FPlan plan, FPlan dbPlan) {



        if (hasChanged(plan.gemeinde, dbPlan.gemeinde)) {
            logger.info(String.format("<>gemeinde %s %s", toString(plan.gemeinde), toString(dbPlan.gemeinde)));
            return true;
        }
        if (hasChanged(plan.getExternereferenzes(), dbPlan.getExternereferenzes())) {
            logger.info(String.format("<>Externereferenzes %s %s", plan.getExternereferenzes(), dbPlan.getExternereferenzes()));
            return true;
        }

        if (plan.geom == null || dbPlan.geom == null) {
            throw new IllegalArgumentException("one Plan doesnt has a geometry");
        }
        if (!plan.geom.equals(dbPlan.geom)) {
            logger.info(String.format("<>geom %s %s", plan.geom, dbPlan.geom));
            return true;
        }
        if (hasChanged(plan.wirksamkeitsdatum, dbPlan.wirksamkeitsdatum)) {
            logger.info(String.format("<>inkrafttretensdatum %s %s", plan.wirksamkeitsdatum, dbPlan.wirksamkeitsdatum));
            return true;
        }
        if (hasChanged(plan.auslegungsstartdatum, dbPlan.auslegungsstartdatum)) {
            logger.info(String.format("<>auslegungsstartdatum %s %s", plan.auslegungsstartdatum, dbPlan.auslegungsstartdatum));
            return true;
        }
        if (hasChanged(plan.auslegungsenddatum, dbPlan.auslegungsenddatum)) {
            logger.info(String.format("<>auslegungsenddatum %s %s", plan.auslegungsenddatum, dbPlan.auslegungsenddatum));
            return true;
        }  
        if (hasChanged(plan.name, dbPlan.name)) {
            logger.info(String.format("<>name %s %s", plan.name, dbPlan.name));
            return true;
        }
        if (hasChanged(plan.nummer, dbPlan.nummer)) {
            logger.info(String.format("<>nummer %s %s", plan.nummer, dbPlan.nummer));
            return true;
        }
        if (hasChanged(plan.planart, dbPlan.planart)) {
            logger.info(String.format("<>planart %s %s", plan.planart, dbPlan.planart));
            return true;
        }
        if (hasChanged(plan.rechtsstand, dbPlan.rechtsstand)) {
            logger.info(String.format("<>rechtsstand %s %s", plan.rechtsstand, dbPlan.rechtsstand));
            return true;
        }
        
        if (hasChanged(plan.aendert, dbPlan.aendert)) {
            logger.info(String.format("<>aendert %s %s", plan.aendert, dbPlan.aendert));
            return true;
        }
        if (hasChanged(plan.wurdegeaendertvon, dbPlan.wurdegeaendertvon)) {
            logger.info(String.format("<>wurdegeaendertvon %s %s", plan.wurdegeaendertvon, dbPlan.wurdegeaendertvon));
            return true;
        }
//        if (hasChanged(plan.internalid, dbPlan.internalid)) {
//            logger.info(String.format("<>internalid %s %s", plan.internalid, dbPlan.internalid));
//            return true;
//        }
        return false;
    }    
    
    public static boolean hasChanged(BPlan plan, BPlan dbPlan) {

        if (hasChanged(plan.gemeinde, dbPlan.gemeinde)) {
            logger.info(String.format("<>gemeinde %s %s", toString(plan.gemeinde), toString(dbPlan.gemeinde)));
            return true;
        }
        if (hasChanged(plan.getExternereferenzes(), dbPlan.getExternereferenzes())) {
            logger.info(String.format("<>Externereferenzes %s %s", plan.getExternereferenzes(), dbPlan.getExternereferenzes()));
            return true;
        }

        if (plan.geom == null || dbPlan.geom == null) {
            throw new IllegalArgumentException("one Plan doesnt has a geometry");
        }
        if (!plan.geom.equals(dbPlan.geom)) {
            logger.info(String.format("<>geom %s %s", plan.geom, dbPlan.geom));
            return true;
        }
        if (hasChanged(plan.inkrafttretensdatum, dbPlan.inkrafttretensdatum)) {
            logger.info(String.format("<>inkrafttretensdatum %s %s", plan.inkrafttretensdatum, dbPlan.inkrafttretensdatum));
            return true;
        }
        if (hasChanged(plan.auslegungsstartdatum, dbPlan.auslegungsstartdatum)) {
            logger.info(String.format("<>auslegungsstartdatum %s %s", plan.auslegungsstartdatum, dbPlan.auslegungsstartdatum));
            return true;
        }
        if (hasChanged(plan.auslegungsenddatum, dbPlan.auslegungsenddatum)) {
            logger.info(String.format("<>auslegungsenddatum %s %s", plan.auslegungsenddatum, dbPlan.auslegungsenddatum));
            return true;
        }  
        if (hasChanged(plan.name, dbPlan.name)) {
            logger.info(String.format("<>name %s %s", plan.name, dbPlan.name));
            return true;
        }
        if (hasChanged(plan.nummer, dbPlan.nummer)) {
            logger.info(String.format("<>nummer %s %s", plan.nummer, dbPlan.nummer));
            return true;
        }
        if (!Arrays.equals(plan.planart, dbPlan.planart)) {
            logger.info(String.format("<>planart %s %s", plan.planart, dbPlan.planart));
            return true;
        }
        if (hasChanged(plan.rechtsstand, dbPlan.rechtsstand)) {
            logger.info(String.format("<>rechtsstand %s %s", plan.rechtsstand, dbPlan.rechtsstand));
            return true;
        }
        
        if (hasChanged(plan.aendert, dbPlan.aendert)) {
            logger.info(String.format("<>aendert %s %s", plan.aendert, dbPlan.aendert));
            return true;
        }
        if (hasChanged(plan.wurdegeaendertvon, dbPlan.wurdegeaendertvon)) {
            logger.info(String.format("<>wurdegeaendertvon %s %s", plan.wurdegeaendertvon, dbPlan.wurdegeaendertvon));
            return true;
        }
        if (hasChanged(plan.internalid, dbPlan.internalid)) {
            logger.info(String.format("<>internalid %s %s", plan.internalid, dbPlan.internalid));
            return true;
        }
//        "status",
        if (hasChanged(plan.status, dbPlan.status)) {
            logger.info(String.format("<>status %s %s", plan.status, dbPlan.status));
            return true;
        }
//        "verfahren",
        if (hasChanged(plan.verfahren, dbPlan.verfahren)) {
            logger.info(String.format("<>verfahren %s %s", plan.verfahren, dbPlan.verfahren));
            return true;
        }
//        "untergangsdatum",
        if (hasChanged(plan.untergangsdatum, dbPlan.untergangsdatum)) {
            logger.info(String.format("<>untergangsdatum %s %s", plan.untergangsdatum, dbPlan.untergangsdatum));
            return true;
        }
//        "genehmigungsdatum",
        if (hasChanged(plan.genehmigungsdatum, dbPlan.genehmigungsdatum)) {
            logger.info(String.format("<>genehmigungsdatum %s %s", plan.genehmigungsdatum, dbPlan.genehmigungsdatum));
            return true;
        }
//        "gruenordnungsplan",
        if (hasChanged(plan.gruenordnungsplan, dbPlan.gruenordnungsplan)) {
            logger.info(String.format("<>gruenordnungsplan %s %s", plan.gruenordnungsplan, dbPlan.gruenordnungsplan));
            return true;
        }
//        "ausfertigungsdatum",
        if (hasChanged(plan.ausfertigungsdatum, dbPlan.ausfertigungsdatum)) {
            logger.info(String.format("<>ausfertigungsdatum %s %s", plan.ausfertigungsdatum, dbPlan.ausfertigungsdatum));
            return true;
        }        
//        "durchfuehrungsvertrag",
        if (hasChanged(plan.durchfuehrungsvertrag, dbPlan.durchfuehrungsvertrag)) {
            logger.info(String.format("<>durchfuehrungsvertrag %s %s", plan.durchfuehrungsvertrag, dbPlan.durchfuehrungsvertrag));
            return true;
        }         
//        "erschliessungsvertrag",
        if (hasChanged(plan.erschliessungsvertrag, dbPlan.erschliessungsvertrag)) {
            logger.info(String.format("<>erschliessungsvertrag %s %s", plan.erschliessungsvertrag, dbPlan.erschliessungsvertrag));
            return true;
        }        
//        "rechtsverordnungsdatum",
        if (hasChanged(plan.rechtsverordnungsdatum, dbPlan.rechtsverordnungsdatum)) {
            logger.info(String.format("<>rechtsverordnungsdatum %s %s", plan.rechtsverordnungsdatum, dbPlan.rechtsverordnungsdatum));
            return true;
        }              
//        "satzungsbeschlussdatum",
        if (hasChanged(plan.satzungsbeschlussdatum, dbPlan.satzungsbeschlussdatum)) {
            logger.info(String.format("<>satzungsbeschlussdatum %s %s", plan.satzungsbeschlussdatum, dbPlan.satzungsbeschlussdatum));
            return true;
        }         
//        "staedtebaulichervertrag",
        if (hasChanged(plan.staedtebaulichervertrag, dbPlan.staedtebaulichervertrag)) {
            logger.info(String.format("<>staedtebaulichervertrag %s %s", plan.staedtebaulichervertrag, dbPlan.staedtebaulichervertrag));
            return true;
        }
////        "veroeffentlichungsdatum",
//        "planaufstellendegemeinde",
        if (hasChanged(plan.planaufstellendegemeinde, dbPlan.planaufstellendegemeinde)) {
            logger.info(String.format("<>planaufstellendegemeinde %s %s", plan.planaufstellendegemeinde, dbPlan.planaufstellendegemeinde));
            return true;
        }
//        "veraenderungssperredatum",
        if (hasChanged(plan.veraenderungssperredatum, dbPlan.veraenderungssperredatum)) {
            logger.info(String.format("<>veraenderungssperredatum %s %s", plan.veraenderungssperredatum, dbPlan.veraenderungssperredatum));
            return true;
        }
//        "aufstellungsbeschlussdatum",
        if (hasChanged(plan.aufstellungsbeschlussdatum, dbPlan.aufstellungsbeschlussdatum)) {
            logger.info(String.format("<>aufstellungsbeschlussdatum %s %s", plan.aufstellungsbeschlussdatum, dbPlan.aufstellungsbeschlussdatum));
            return true;
        }
//        "traegerbeteiligungsenddatum",
        if (hasChanged(plan.traegerbeteiligungsenddatum, dbPlan.traegerbeteiligungsenddatum)) {
            logger.info(String.format("<>traegerbeteiligungsenddatum %s %s", plan.traegerbeteiligungsenddatum, dbPlan.traegerbeteiligungsenddatum));
            return true;
        }
//        "veraenderungssperreenddatum",
        if (hasChanged(plan.veraenderungssperreenddatum, dbPlan.veraenderungssperreenddatum)) {
            logger.info(String.format("<>veraenderungssperreenddatum %s %s", plan.veraenderungssperreenddatum, dbPlan.veraenderungssperreenddatum));
            return true;
        }
//        "traegerbeteiligungsstartdatum",
        if (hasChanged(plan.traegerbeteiligungsstartdatum, dbPlan.traegerbeteiligungsstartdatum)) {
            logger.info(String.format("<>traegerbeteiligungsstartdatum %s %s", plan.traegerbeteiligungsstartdatum, dbPlan.traegerbeteiligungsstartdatum));
            return true;
        }
//        "verlaengerungveraenderungssperre",
        if (hasChanged(plan.verlaengerungveraenderungssperre, dbPlan.verlaengerungveraenderungssperre)) {
            logger.info(String.format("<>verlaengerungveraenderungssperre %s %s", plan.verlaengerungveraenderungssperre, dbPlan.verlaengerungveraenderungssperre));
            return true;
        }
//        "veraenderungssperrebeschlussdatum",
        if (hasChanged(plan.veraenderungssperrebeschlussdatum, dbPlan.veraenderungssperrebeschlussdatum)) {
            logger.info(String.format("<>veraenderungssperrebeschlussdatum %s %s", plan.veraenderungssperrebeschlussdatum, dbPlan.veraenderungssperrebeschlussdatum));
            return true;
        }
        return false;
    }    
    
    
    static private boolean hasChangedXPBereich(XPBereich<?> o1, XPBereich<?> o2) {        
        if (!Objects.equals(o1.gml_id, o2.gml_id)) {
            logger.info(String.format("<>gml_id %s %s", o1.gml_id, o2.gml_id));
            return true;
        }
//      nummer integer NOT NULL,       
        if (!Objects.equals(o1.nummer, o2.nummer)) {
            logger.info(String.format("<>nummer %s %s", o1.nummer, o2.nummer));
            return true;
        }
//      name character varying COLLATE pg_catalog."default",
        if (!Objects.equals(o1.name, o2.name)) {
            logger.info(String.format("<>name %s %s", o1.name, o2.name));
            return true;
        }        
//      bedeutung xplan_gml.xp_bedeutungenbereich
        if (!Objects.equals(o1.bedeutung, o2.bedeutung)) {
            logger.info(String.format("<>bedeutung %s %s", o1.bedeutung, o2.bedeutung));
            return true;
        }
//      detailliertebedeutung character varying COLLATE pg_catalog."default",
        if (!Objects.equals(o1.detailliertebedeutung, o2.detailliertebedeutung)) {
            logger.info(String.format("<>detailliertebedeutung %s %s", o1.detailliertebedeutung, o2.detailliertebedeutung));
            return true;
        }
//      erstellungsmassstab integer,
        if (!Objects.equals(o1.erstellungsmassstab, o2.erstellungsmassstab)) {
            logger.info(String.format("<>erstellungsmassstab %s %s", o1.erstellungsmassstab, o2.erstellungsmassstab));
            return true;
        }
//      geltungsbereich geometry(MultiPolygon),
        if (!Objects.equals(o1.geltungsbereich, o2.geltungsbereich)) {
            logger.info(String.format("<>geltungsbereich %s %s", o1.geltungsbereich, o2.geltungsbereich));
            return true;
        }        
//      user_id integer,
        if (!Objects.equals(o1.user_id, o2.user_id)) {
            logger.info(String.format("<>user_id %s %s", o1.user_id, o2.user_id));
            return true;
        }
//      created_at timestamp without time zone NOT NULL DEFAULT now(),        
        if (!Objects.equals(o1.created_at, o2.created_at)) {
            logger.info(String.format("<>created_at %s %s", o1.created_at, o2.created_at));
            return true;
        }
//      updated_at timestamp without time zone NOT NULL DEFAULT now(),
        if (!Objects.equals(o1.updated_at, o2.updated_at)) {
            logger.info(String.format("<>updated_at %s %s", o1.updated_at, o2.updated_at));
            return true;
        }
//      konvertierung_id integer,
        if (!Objects.equals(o1.konvertierung_id, o2.konvertierung_id)) {
            logger.info(String.format("<>konvertierung_id %s %s", o1.konvertierung_id, o2.konvertierung_id));
            return true;
        }
//       planinhalt text COLLATE pg_catalog."default",        
        if (!Objects.equals(o1.planinhalt, o2.planinhalt)) {
            logger.info(String.format("<>planinhalt %s %s", o1.planinhalt, o2.planinhalt));
            return true;
        }
//      praesentationsobjekt text COLLATE pg_catalog."default",
        if (!Objects.equals(o1.praesentationsobjekt, o2.praesentationsobjekt)) {
            logger.info(String.format("<>praesentationsobjekt %s %s", o1.praesentationsobjekt, o2.praesentationsobjekt));
            return true;
        }
//      rasterbasis text COLLATE pg_catalog."default",
        if (!Objects.equals(o1.rasterbasis, o2.rasterbasis)) {
            logger.info(String.format("<>rasterbasis %s %s", o1.rasterbasis, o2.rasterbasis));
            return true;
        }
//      refscan xplan_gml.xp_externereferenz[],
        if (!Arrays.equals(o1.refscan, o2.refscan)) {
            logger.info(String.format("<>refscan %s %s", o1.refscan, o2.refscan));
            return true;
        }
        return false;
    }    
    
    public static <T extends XPBereich<?>>boolean hasChanged(T bereich, T dbBereich) {
        if (bereich instanceof FPBereich && dbBereich instanceof FPBereich) {
            return hasChanged((FPBereich)bereich, (FPBereich)dbBereich);
        }
        if (bereich instanceof SOBereich && dbBereich instanceof SOBereich) {
            return hasChanged((SOBereich)bereich, (SOBereich)dbBereich);
        }
        if (bereich instanceof BPBereich && dbBereich instanceof BPBereich) {
            return hasChanged((BPBereich)bereich, (BPBereich)dbBereich);
        }
        throw new IllegalArgumentException("Parameter not both from the same Class or not Instances of BPlan|FPlan|SOPlan");
        
    }    

    static public boolean hasChanged(FPBereich o1, FPBereich o2) {
        if (HasChangedFunctions.hasChangedXPBereich(o1, o2)) {
            return true;
        }        
//      LocalDate versionbaunvodatum;
        if (!Objects.equals(o1.versionbaunvodatum, o2.versionbaunvodatum)) {
            logger.info(String.format("<>versionbaunvodatum %s %s", o1.versionbaunvodatum, o2.versionbaunvodatum));
            return true;
        } 
//      String versionbaugbtext;
        if (!Objects.equals(o1.versionbaugbtext, o2.versionbaugbtext)) {
            logger.info(String.format("<>versionbaugbtext %s %s", o1.versionbaugbtext, o2.versionbaugbtext));
            return true;
        }
//      String versionsonstrechtsgrundlagetext;
        if (!Objects.equals(o1.versionsonstrechtsgrundlagetext, o2.versionsonstrechtsgrundlagetext)) {
            logger.info(String.format("<>versionsonstrechtsgrundlagetext %s %s", o1.versionsonstrechtsgrundlagetext, o2.versionsonstrechtsgrundlagetext));
            return true;
        }
//      String versionbaunvotext;
        if (!Objects.equals(o1.versionbaunvotext, o2.versionbaunvotext)) {
            logger.info(String.format("<>versionbaunvotext %s %s", o1.versionbaunvotext, o2.versionbaunvotext));
            return true;
        }
//      LocalDate versionsonstrechtsgrundlagedatum;
        if (!Objects.equals(o1.versionsonstrechtsgrundlagedatum, o2.versionsonstrechtsgrundlagedatum)) {
            logger.info(String.format("<>versionsonstrechtsgrundlagedatum %s %s", o1.versionsonstrechtsgrundlagedatum, o2.versionsonstrechtsgrundlagedatum));
            return true;
        }
//      LocalDate versionbaugbdatum;
        if (!Objects.equals(o1.versionbaugbdatum, o2.versionbaugbdatum)) {
            logger.info(String.format("<>versionbaugbdatum %s %s", o1.versionbaugbdatum, o2.versionbaugbdatum));
            return true;
        }
//      UUID gehoertzuplan;
        if (!Objects.equals(o1.gehoertzuplan, o2.gehoertzuplan)) {
            logger.info(String.format("<>gehoertzuplan %s %s", o1.gehoertzuplan, o2.gehoertzuplan));
            return true;
        }
        return false;
    }      
    
    static public boolean hasChanged(SOBereich o1, SOBereich o2) {
        if (HasChangedFunctions.hasChangedXPBereich(o1, o2)) {
            return true;
        }        
        return false;
    }       
    
    static public boolean hasChanged(BPBereich o1, BPBereich o2) {
        if (HasChangedFunctions.hasChangedXPBereich(o1, o2)) {
            return true;
        }        
//      LocalDate versionbaunvodatum;
        if (!Objects.equals(o1.versionbaunvodatum, o2.versionbaunvodatum)) {
            logger.info(String.format("<>versionbaunvodatum %s %s", o1.versionbaunvodatum, o2.versionbaunvodatum));
            return true;
        } 
//      String versionbaugbtext;
        if (!Objects.equals(o1.versionbaugbtext, o2.versionbaugbtext)) {
            logger.info(String.format("<>versionbaugbtext %s %s", o1.versionbaugbtext, o2.versionbaugbtext));
            return true;
        }
//      String versionsonstrechtsgrundlagetext;
        if (!Objects.equals(o1.versionsonstrechtsgrundlagetext, o2.versionsonstrechtsgrundlagetext)) {
            logger.info(String.format("<>versionsonstrechtsgrundlagetext %s %s", o1.versionsonstrechtsgrundlagetext, o2.versionsonstrechtsgrundlagetext));
            return true;
        }
//      String versionbaunvotext;
        if (!Objects.equals(o1.versionbaunvotext, o2.versionbaunvotext)) {
            logger.info(String.format("<>versionbaunvotext %s %s", o1.versionbaunvotext, o2.versionbaunvotext));
            return true;
        }
//      LocalDate versionsonstrechtsgrundlagedatum;
        if (!Objects.equals(o1.versionsonstrechtsgrundlagedatum, o2.versionsonstrechtsgrundlagedatum)) {
            logger.info(String.format("<>versionsonstrechtsgrundlagedatum %s %s", o1.versionsonstrechtsgrundlagedatum, o2.versionsonstrechtsgrundlagedatum));
            return true;
        }
//      LocalDate versionbaugbdatum;
        if (!Objects.equals(o1.versionbaugbdatum, o2.versionbaugbdatum)) {
            logger.info(String.format("<>versionbaugbdatum %s %s", o1.versionbaugbdatum, o2.versionbaugbdatum));
            return true;
        }
//      UUID gehoertzuplan;
        if (!Objects.equals(o1.gehoertzuplan, o2.gehoertzuplan)) {
            logger.info(String.format("<>gehoertzuplan %s %s", o1.gehoertzuplan, o2.gehoertzuplan));
            return true;
        }
        return false;
    }    
    
    private static boolean hasChanged(CodeList o1, CodeList o2) {
        if (o1==null) {
            if (o2!=null) {
                return true;
            }
            return false;
        }
        CodeList cl01 = (CodeList)o1;
        CodeList cl02 = (CodeList)o2;
        if (hasChanged(cl01.getCodespace(), o2.getCodespace())) {
            return true;
        }
        return (hasChanged(cl01.getCodelistValue(), cl02.getCodelistValue()));
    }    
    
    public static boolean hasChanged(PGVerbundenerPlan[] a01, PGVerbundenerPlan[] a02) {
        if (a01 == null) {
            return (a02 == null) ? false : true;
        }
        if (a02 == null) {
            return true;
        }
        if (a01.length != a02.length) {
            return true;
        }
        for (int i=0; i<a01.length; i++) {
    
            VerbundenerPlan vb01 = a01[i].getVerbundenerPlan();
            VerbundenerPlan vb02 = a02[i].getVerbundenerPlan();
            if (hasChanged(vb01.getPlanname(), vb02.getPlanname())) {
                logger.info(String.format("<>PGVerbundenerPlan[]planName %s %s", vb01.getPlanname(), vb02.getPlanname()));
                return true;
            }
            if (hasChanged(vb01.getNummer(), vb02.getNummer())) {
                logger.info(String.format("<>PGVerbundenerPlan[].nummer %s %s", vb01.getNummer(), vb02.getNummer()));
                return true;
            }
            if (hasChanged(vb01.getRechtscharakter(), vb02.getRechtscharakter())) {
                logger.info(String.format("<>PGVerbundenerPlan[].rechtscharakter %s %s", vb01.getRechtscharakter(), vb02.getRechtscharakter()));
                return true;
            }
            if (hasChanged(vb01.getVerbundenerplan(), vb02.getVerbundenerplan())) {
                logger.info(String.format("<>PGVerbundenerPlan[].verbundenerplan %s %s", vb01.getVerbundenerplan(), vb02.getVerbundenerplan()));
                return true;
            }
        }
        return false;
    }

    public static boolean hasChanged(PGSpezExterneReferenz[] a01, PGSpezExterneReferenz[] a02) {
        if (a01 == null) {
            return (a02 == null) ? false : true;
        }
        if (a02 == null) {
            return true;
        }
        if (a01.length != a02.length) {
            logger.info(String.format("<>PGExterneReferenz.length %s %s", a01.length, a02.length));
            return true;
        }
        for (int i=0; i<a01.length; i++) {
    
            SpezExterneRef exRef01 = a01[i].object;
            SpezExterneRef exRef02 = a02[i].object;
            
            if (hasChanged(exRef01.art, exRef02.art)) {                
                logger.info(String.format("<>PGExterneReferenz.art idx=%s %s %s", i, exRef01.art, exRef02.art));
                return true;
            }
            if (hasChanged(exRef01.beschreibung, exRef02.beschreibung)) {
                String s1 = (exRef01.beschreibung==null ? "null" : ("\""+exRef01.beschreibung+"\""));
                String s2 = (exRef02.beschreibung==null ? "null" : ("\""+exRef02.beschreibung+"\""));
                logger.info(String.format("<>PGExterneReferenz.beschreibung idx=%s %s %s", i, s1, s2));
                return true;
            }
            if (hasChanged(exRef01.datum, exRef02.datum)) {
                logger.info(String.format("<>PGExterneReferenz.datum idx=%s %s %s", i, exRef01.datum, exRef02.datum));
                return true;        
            }
            if (hasChanged(exRef01.georefmimetype, exRef02.georefmimetype)) {
                logger.info(String.format("<>PGExterneReferenz.georefmimetype idx=%s %s %s", i, exRef01.georefmimetype, exRef02.georefmimetype));
                return true;
            } 
            if (hasChanged(exRef02.georefurl, exRef02.georefurl)) {
                logger.info(String.format("<>PGExterneReferenz.georefurl idx=%s %s %s", i, exRef01.georefurl, exRef02.georefurl));
                return true;
            } 
            if (hasChanged(exRef01.informationssystemurl, exRef02.informationssystemurl)) {
                logger.info(String.format("<>PGExterneReferenz.informationssystemurl idx=%s %s %s", i, exRef01.informationssystemurl, exRef02.informationssystemurl));
                return true;
            } 
            if (hasChanged(exRef01.referenzname, exRef02.referenzname)) {
                logger.info(String.format("<>PGExterneReferenz.referenzname idx=%s %s %s", i, exRef01.referenzname, exRef02.referenzname));
                return true;                    
            }
            if (hasChanged(exRef01.referenzurl, exRef02.referenzurl)) {
                logger.info(String.format("<>PGExterneReferenz.referenzurl idx=%s %s %s", i, exRef01.referenzurl, exRef02.referenzurl));
                return true;
            } 
            if (hasChanged(exRef01.typ, exRef02.typ)) {
                logger.info(String.format("<>PGExterneReferenz.typ idx=%s %s %s", i, exRef01.typ, exRef02.typ));
                return true;
            }
        }
        return false;
    }
    
    public static boolean hasChanged(
            de.gdiservice.bplan.poi.Gemeinde[] gemeinden01, 
            de.gdiservice.bplan.poi.Gemeinde[] gemeinden02) {
        if (gemeinden01 == null && gemeinden02 == null) {
            return false;
        }
        if (gemeinden01 == null || gemeinden02 == null) {
            throw new IllegalArgumentException("one Plan doesnt has gemeinden");
        }
        if (gemeinden01 == null || gemeinden02 == null) {
            return true;                                                                                                                                                 
        }
        if (gemeinden01.length != gemeinden02.length) {
            return true;
        }
        for (int i=0; i<gemeinden01.length; i++) {
            if (!gemeinden01[i].equals(gemeinden02[i])) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean hasChanged(Object o1, Object o2) {
        if (o1==null) {
            if (o2!=null) {
                return true;
            }
            return false;
        }
        return !o1.equals(o2);
    }
    
    private static boolean hasChanged(Object[] o1, Object[] o2) {
        if (o1==null) {
            if (o2!=null) {
                return true;
            }
            return false;
        }
        return !Arrays.equals(o1, o2);
    }
    
    private static String toString(de.gdiservice.bplan.poi.Gemeinde[] gemeinden) {
        StringBuilder sb = new StringBuilder();
        if (gemeinden != null) {
            for (int i=0; i<gemeinden.length; i++) {
                sb.append("\n\ttoString[\"").append(gemeinden[i]).append("\"\n\t");
                sb.append("toStrin2[ags=").append(gemeinden[i].ags).append(" rs=").append(gemeinden[i].rs);
                sb.append(" gemeindename=\"").append(gemeinden[i].gemeindename).append("\"");
                sb.append(" ortsteilname=\"").append(gemeinden[i].ortsteilname).append("\"");
            }
        } else {
            sb.append("null");
        }
        return sb.toString();
    }
}
