package online.flowerinsnow.forcedeath.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;

public class ForceDeathCommand {
    private static final Random RANDOM = new Random();

    private static final DynamicCommandExceptionType UNABLE_TO_MAKE_ENTITY_DEATH = new DynamicCommandExceptionType(
            arg -> new TranslationTextComponent("commands.forcedeath.failed.simple", arg)
    );

    private static final DynamicCommandExceptionType UNABLE_TO_MAKE_ENTITIES_DEATH = new DynamicCommandExceptionType(
            arg -> new TranslationTextComponent("commands.forcedeath.failed.multiple", arg)
    );

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("forcedeath")
                .requires(source ->
                    source.hasPermissionLevel(2)
                ).executes(cmd ->
                    makeDeath(cmd.getSource(), ImmutableList.of(cmd.getSource().assertIsEntity()))
                ).then(Commands.argument("targets", EntityArgument.entities())
                    .executes(cmd ->
                        makeDeath(cmd.getSource(), EntityArgument.getEntities(cmd, "targets"))
                )));
    }

    private static int makeDeath(CommandSource source, Collection<? extends Entity> entities) throws CommandSyntaxException {
        List<Entity> success = new ArrayList<>();
        List<Entity> failed = new ArrayList<>();
        entities.forEach(var0 -> {
            if (var0 instanceof LivingEntity) {
                LivingEntity entity = (LivingEntity) var0;
                List<DamageSource> list = new ArrayList<>(Arrays.asList(
                        DamageSource.IN_FIRE,
                        DamageSource.LIGHTNING_BOLT,
                        DamageSource.ON_FIRE,
                        DamageSource.LAVA,
                        DamageSource.HOT_FLOOR,
                        DamageSource.IN_WALL,
                        DamageSource.CRAMMING,
                        DamageSource.DROWN,
                        DamageSource.STARVE,
                        DamageSource.CACTUS,
                        DamageSource.FALL,
                        DamageSource.FLY_INTO_WALL,
                        DamageSource.OUT_OF_WORLD,
                        DamageSource.MAGIC,
                        DamageSource.WITHER,
                        DamageSource.ANVIL,
                        DamageSource.FALLING_BLOCK,
                        DamageSource.DRAGON_BREATH,
                        DamageSource.DRYOUT,
                        DamageSource.SWEET_BERRY_BUSH
                ));
                if (entity instanceof PlayerEntity) {
                    list.add(DamageSource.causePlayerDamage((PlayerEntity) entity));
                }
                /*
                entity.setHealth(0.0F);
                 */
                DamageSource ds = list.get(RANDOM.nextInt(list.size()));
                entity.getCombatTracker().trackDamage(ds, Float.MAX_VALUE, Float.MAX_VALUE);
                entity.setHealth(0.0F);
                entity.onDeath(ds);
                success.add(var0);
            } else {
                failed.add(var0);
            }
        });
        if (success.size() == 1) {
            source.sendFeedback(new TranslationTextComponent("commands.forcedeath.success.simple", success.get(0).getDisplayName()), true);
        } else if (success.size() > 1) {
            source.sendFeedback(new TranslationTextComponent("commands.forcedeath.success.multiple", success.size()), true);
        }

        if (failed.size() == 1) {
            throw UNABLE_TO_MAKE_ENTITY_DEATH.create(failed.get(0).getDisplayName());
        } else if (failed.size() > 1) {
            throw UNABLE_TO_MAKE_ENTITIES_DEATH.create(failed.size());
        }
        return success.size();
    }
}
