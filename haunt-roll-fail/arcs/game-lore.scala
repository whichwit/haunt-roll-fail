package arcs
//
//
//
//
import hrf.colmat._
import hrf.logger._
//
//
//
//

import hrf.tracker4._
import hrf.tracker4.implicits._
import hrf.elem._

import arcs.elem._


abstract class Lore(val id : String, val name : String) extends Record with Effect with Elementary {
    def img = Image(id, styles.card)
    def elem = name.styled(styles.title).hl
}

case object MirrorPlating     extends Lore("lore04", "Mirror Plating")
case object HiddenHarbors     extends Lore("lore05", "Hidden Harbors")
case object SignalBreaker     extends Lore("lore06", "Signal Breaker")
case object RepairDrones      extends Lore("lore06", "Repair Drones")
case object LivingStructures  extends Lore("lore10", "Living Structures")
case object RailgunArrays     extends Lore("lore12", "Railgun Arrays")
case object SeekerTorpedoes   extends Lore("lore14", "Seeker Torpedoes")
case object AncientHoldings   extends Lore("lore13", "Ancient Holdings")
case object SurvivalOverrides extends Lore("lore18", "Survival Overrides")
case object EmpathsVision     extends Lore("lore19", "Empath's Vision")
case object KeepersTrust      extends Lore("lore21", "Keeper's Trust")
case object WarlordsCruelty   extends Lore("lore23", "Warlord's Cruelty")
case object TyrantsEgo        extends Lore("lore25", "Tyrant's Ego")
case object TycoonsCharm      extends Lore("lore28", "Tycoon's Charm")

object Lores {
    def all = $(
        MirrorPlating     ,
        HiddenHarbors     ,
        SignalBreaker     ,
        RepairDrones      ,
        LivingStructures  ,
        RailgunArrays     ,
        SeekerTorpedoes   ,
        AncientHoldings   ,
        SurvivalOverrides ,
        EmpathsVision     ,
        KeepersTrust      ,
        WarlordsCruelty   ,
        TyrantsEgo        ,
        TycoonsCharm      ,
    )

    def preset1 = $(MirrorPlating, HiddenHarbors, WarlordsCruelty, AncientHoldings, SignalBreaker)
    def preset2 = $(LivingStructures, SurvivalOverrides)
    def preset3 = $()
}

case class DiscardLoreCardAction(self : Faction, c : Lore, then : ForcedAction) extends ForcedAction
case class NurtureMainAction(self : Faction, cost : Cost, then : ForcedAction) extends ForcedAction with Soft
case class MartyrMainAction(self : Faction, cost : Cost, then : ForcedAction) extends ForcedAction with Soft
case class MartyrAction(self : Faction, cost : Cost, s : System, u : Figure, t : Figure, then : ForcedAction) extends ForcedAction


object LoreExpansion extends Expansion {
    def perform(action : Action, soft : Void)(implicit game : Game) = action @@ {
        case DiscardLoreCardAction(f, c, then) =>
            f.lores :-= c

            f.log("discarded", c)

            then

        // LIVING STRUCTURES
        case NurtureMainAction(f, x, then) =>
            val g = "Tax".hl

            Ask(f).group(g)
                .some(systems)(s => f.at(s).cities./(c => TaxAction(f, x, s, c, true, then).as(c, "in", s, |(board.resource(s)).%(game.available)./(r => ("for", r, Image(r.name, styles.token))))(g).!(f.taxed.has(c), "taxed")))
                .cancel

        // SURVIVAL OVERRIDES
        case MartyrMainAction(f, x, then) =>
            def convert(u : Figure) = game.showFigure(u, u.damaged.??(1))

            Ask(f).group("Martyr".hl)
                .some(systems./~(s => f.at(s).ships.fresh.take(1)./(s -> _))) { case (s, u) =>
                    f.others./~(_.at(s).ships./(t => MartyrAction(f, x, s, u, t, then).as(convert(t))("Martyr".hh, "in", s)))
                }
                .cancel

        case MartyrAction(f, x, s, u, t, then) =>
            f.pay(x)

            t --> f.trophies

            u --> f.reserve

            f.log("martyred", t, "in", s, "with", u)

            then

        case _ => UnknownContinue
    }
}

