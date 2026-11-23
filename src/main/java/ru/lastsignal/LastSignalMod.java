package ru.lastsignal;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import ru.lastsignal.GuardEntity;
import ru.lastsignal.OrlovEntity;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import static net.minecraft.server.command.CommandManager.literal;

public final class LastSignalMod implements ModInitializer {
    private static final String ID = "lastsignal";
    private static final String STAGE = "lastsignal.stage.";
    private static final String X = "lastsignal.x.";
    private static final String Y = "lastsignal.y.";
    private static final String Z = "lastsignal.z.";
    private static final String ORLOV = "lastsignal.orlov";
    private static final String ENEMY = "lastsignal.enemy";
    private static final Item KEYCARD = Registry.register(Registries.ITEM,
            Identifier.of(ID, "keycard"), new Item(new Item.Settings().maxCount(1)));

    @Override public void onInitialize() {
        LastSignalContent.initialize();
        CommandRegistrationCallback.EVENT.register((d, r, e) -> commands(d));
        UseEntityCallback.EVENT.register(this::entityUse);
        UseBlockCallback.EVENT.register(this::blockUse);
        ServerTickEvents.END_SERVER_TICK.register(this::tick);
    }

    private void commands(CommandDispatcher<ServerCommandSource> d) {
        d.register(literal("lastsignal")
            .then(literal("start").executes(c -> { start(c.getSource().getPlayerOrThrow()); return 1; }))
            .then(literal("status").executes(c -> { status(c.getSource().getPlayerOrThrow()); return 1; }))
            .then(literal("choice")
                .then(literal("transmit").executes(c -> { finish(c.getSource().getPlayerOrThrow(), true); return 1; }))
                .then(literal("destroy").executes(c -> { finish(c.getSource().getPlayerOrThrow(), false); return 1; })))
            .then(literal("reset").executes(c -> { reset(c.getSource().getPlayerOrThrow()); return 1; })));
    }

    private void start(ServerPlayerEntity p) {
        if (stage(p) > 0 && stage(p) < 7) { say(p, "§cМиссия уже начата. Используй /lastsignal status."); return; }
        clear(p);
        BlockPos o = p.getBlockPos().down();
        saveOrigin(p, o);
        setStage(p, 1);
        ServerWorld w = (ServerWorld)p.getWorld();
        build(w, o);
        OrlovEntity v = LastSignalContent.ORLOV.create(w);
        if (v != null) {
            v.refreshPositionAndAngles(o.getX()+3.5, o.getY()+1, o.getZ()+.5, 90, 0);
            v.setCustomName(Text.literal("§bДоктор Орлов"));
            v.setCustomNameVisible(true);
            v.setInvulnerable(true);
            v.setAiDisabled(true);
            v.addCommandTag(ORLOV);
            w.spawnEntity(v);
        }
        say(p, "§8[§cПОСЛЕДНИЙ СИГНАЛ§8] §fСигнал бедствия принят. Источник — военный объект «Север-9».");
        say(p, "§7В комплексе обнаружен один живой сотрудник. Найди доктора Орлова.");
        say(p, "§eЦель: поговорить с доктором Орловым.");
        if (FabricLoader.getInstance().isModLoaded("tacz"))
            say(p, "§6TaCZ обнаружен. Для зачистки можешь использовать своё оружие и боеприпасы.");
    }

    private void build(ServerWorld w, BlockPos o) {
        for (int x=-3; x<=29; x++) for (int z=-5; z<=5; z++) {
            w.setBlockState(o.add(x,0,z), Blocks.DEEPSLATE_TILES.getDefaultState());
            if (x==-3 || x==29 || z==-5 || z==5)
                for (int y=1; y<=4; y++) w.setBlockState(o.add(x,y,z), Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState());
        }
        for (int x : new int[]{0,7,14,21,28}) {
            w.setBlockState(o.add(x,4,-4), Blocks.SEA_LANTERN.getDefaultState());
            w.setBlockState(o.add(x,4,4), Blocks.SEA_LANTERN.getDefaultState());
        }
        w.setBlockState(o.add(9,1,0), LastSignalContent.TERMINAL.getDefaultState());
        w.setBlockState(o.add(20,1,0), Blocks.LODESTONE.getDefaultState());
        w.setBlockState(o.add(27,1,0), Blocks.LODESTONE.getDefaultState());
        for (int x : new int[]{14,17,23}) {
            w.setBlockState(o.add(x,1,-2), Blocks.IRON_BLOCK.getDefaultState());
            w.setBlockState(o.add(x,2,-2), Blocks.IRON_BLOCK.getDefaultState());
            w.setBlockState(o.add(x,1,2), Blocks.IRON_BLOCK.getDefaultState());
        }
    }

    private ActionResult entityUse(net.minecraft.entity.player.PlayerEntity p, World w, Hand h, Entity e, EntityHitResult hit) {
        if (w.isClient || !(p instanceof ServerPlayerEntity s) || !e.getCommandTags().contains(ORLOV)) return ActionResult.PASS;
        if (stage(s)==1) {
            say(s, "§bОрлов: §f«Наконец-то. Не снимай шлем — воздух заражён. Охрана заперла центральный архив.»");
            say(s, "§bОрлов: §f«Возьми ключ-карту. Запусти аварийный журнал на первом терминале.»");
            say(s, "§bОрлов: §f«И не верь голосам из динамиков. Протокол научился их подделывать.»");
            s.giveItemStack(new ItemStack(KEYCARD));
            setStage(s,2);
            say(s, "§eЦель: активировать первый терминал с ключ-картой.");
        } else say(s, "§bОрлов: §f«Не стой здесь. Терминалы дальше по коридору. Времени мало.»");
        return ActionResult.SUCCESS;
    }

    private ActionResult blockUse(net.minecraft.entity.player.PlayerEntity p, World w, Hand h, BlockHitResult hit) {
        if (w.isClient || !(p instanceof ServerPlayerEntity s) || !(w.getBlockState(hit.getBlockPos()).isOf(Blocks.LODESTONE) || w.getBlockState(hit.getBlockPos()).isOf(LastSignalContent.TERMINAL)) || stage(s)==0)
            return ActionResult.PASS;
        BlockPos o=origin(s), b=hit.getBlockPos();
        if (near(b,o.add(9,1,0))) {
            if (stage(s)!=2) { say(s,"§7Терминал уже прочитан или пока заблокирован."); return ActionResult.SUCCESS; }
            if (!s.getInventory().contains(new ItemStack(KEYCARD))) { say(s,"§cДоступ запрещён. Нужна ключ-карта Орлова."); return ActionResult.SUCCESS; }
            say(s,"§eТерминал: §f«Карта принята. Архив повреждён. В секторе B вооружённый персонал.»");
            say(s,"§cСистема безопасности: §f«Нарушитель обнаружен. Группа реагирования — открыть огонь.»");
            setStage(s,3); spawnSquad(s); say(s,"§eЦель: уничтожить всех бойцов охраны в секторе B.");
            return ActionResult.SUCCESS;
        }
        if (near(b,o.add(20,1,0))) {
            if (stage(s)==4) {
                say(s,"§eПередатчик: §f«Питание восстановлено. Входящий пакет: ПРОТОКОЛ НОЛЬ.»");
                say(s,"§bОрлов по рации: §f«Они не останавливали эпидемию. Они сохраняли её как оружие.»");
                say(s,"§bОрлов: §f«Отправишь архив — за тобой придут. Но теперь выбор твой.»");
                setStage(s,5); say(s,"§eЦель: добраться до эвакуационной вышки.");
            } else say(s,"§7Передатчик не отвечает. Закончи предыдущую задачу.");
            return ActionResult.SUCCESS;
        }
        if (near(b,o.add(27,1,0))) {
            if (stage(s)==5) {
                setStage(s,6);
                say(s,"§8[§cПОСЛЕДНИЙ СИГНАЛ§8] §fВышка подключена. Последний выбор за тобой.");
                say(s,"§a/lastsignal choice transmit §7— раскрыть архив.");
                say(s,"§c/lastsignal choice destroy §7— уничтожить протокол.");
            } else if (stage(s)<5) say(s,"§7Вышка заблокирована. Сначала восстанови передатчик.");
            else say(s,"§7Канал эвакуации уже активирован.");
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    private void spawnSquad(ServerPlayerEntity p) {
        ServerWorld w=(ServerWorld)p.getWorld(); BlockPos o=origin(p);
        int[][] xy={{15,-3},{17,0},{15,3},{23,0}};
        for(int i=0;i<xy.length;i++) {
            GuardEntity mob=LastSignalContent.GUARD.create(w);
            if(mob==null) continue;
            mob.refreshPositionAndAngles(o.getX()+xy[i][0]+.5,o.getY()+1,o.getZ()+xy[i][1]+.5,180,0);
            mob.addCommandTag(ENEMY);
            mob.setCustomName(Text.literal(i==3?"§cКомандир группы":"§cОхрана «Севера-9»"));
            mob.setCustomNameVisible(i==3);
            w.spawnEntity(mob);
        }
    }

    private void tick(MinecraftServer server) {
        for(ServerPlayerEntity p:server.getPlayerManager().getPlayerList()) if(stage(p)==3) {
            BlockPos o=origin(p); Box area=new Box(o.getX()+12, o.getY(), o.getZ()-5, o.getX()+25, o.getY()+6, o.getZ()+5);
            long left=p.getWorld().getEntitiesByClass(GuardEntity.class,area,e->e.getCommandTags().contains(ENEMY)).size();
            if(left==0) {
                setStage(p,4);
                say(p,"§aСектор B зачищен. В журнале найден путь к аварийному передатчику.");
                say(p,"§eЦель: активировать второй терминал.");
            }
        }
    }

    private void status(ServerPlayerEntity p) {
        String msg=switch(stage(p)) {
            case 0 -> "Кампания не начата. /lastsignal start";
            case 1 -> "Цель: найти доктора Орлова.";
            case 2 -> "Цель: активировать первый терминал с ключ-картой.";
            case 3 -> "Цель: зачистить охрану в секторе B.";
            case 4 -> "Цель: восстановить аварийный передатчик.";
            case 5 -> "Цель: добраться до эвакуационной вышки.";
            case 6 -> "Финал: /lastsignal choice transmit или /lastsignal choice destroy";
            case 7 -> "Кампания завершена. /lastsignal reset — начать заново.";
            default -> "Нет активной миссии.";
        };
        say(p,"§e"+msg);
    }

    private void finish(ServerPlayerEntity p, boolean transmit) {
        if(stage(p)!=6) { say(p,"§cФинальный выбор пока недоступен."); return; }
        setStage(p,7);
        say(p,"§8========== §cПОСЛЕДНИЙ СИГНАЛ §8==========");
        if(transmit) {
            say(p,"§fТы нажимаешь «ПЕРЕДАТЬ». Архив уходит в открытые сети.");
            say(p,"§7Независимые станции подтверждают подлинность файлов. Скрыть правду больше нельзя.");
            say(p,"§bОрлов: §f«Теперь это уже не спрятать. Спасибо. Я дождусь тех, кто придёт за доказательствами.»");
            say(p,"§aКОНЦОВКА «ПРАВДА»: протокол раскрыт, но охота только начинается.");
        } else {
            say(p,"§fТы выбираешь «УНИЧТОЖИТЬ». Архив стирается, передатчик замолкает.");
            say(p,"§7Комплекс погружается в темноту. За стенами впервые становится тихо.");
            say(p,"§bОрлов: §f«Может, так будет лучше. Некоторые двери нельзя открывать второй раз.»");
            say(p,"§aКОНЦОВКА «ТИШИНА»: протокол уничтожен, правда останется под землёй.");
        }
        say(p,"§eКампания «Последний сигнал» завершена.");
    }

    private void reset(ServerPlayerEntity p) {
        ServerWorld w=(ServerWorld)p.getWorld(); BlockPos o=origin(p);
        Box area=new Box(o.getX()-5, o.getY()-2, o.getZ()-7, o.getX()+32, o.getY()+8, o.getZ()+7);
        for(Entity e:w.getOtherEntities(p,area,x->x.getCommandTags().contains(ORLOV)||x.getCommandTags().contains(ENEMY))) e.discard();
        clear(p); say(p,"§7Прогресс сброшен. Запусти /lastsignal start.");
    }

    private int stage(ServerPlayerEntity p) {
        for(int i=0;i<=7;i++) if(p.getCommandTags().contains(STAGE+i)) return i;
        return 0;
    }
    private void setStage(ServerPlayerEntity p,int n) {
        for(String t:p.getCommandTags().toArray(String[]::new)) if(t.startsWith(STAGE)) p.removeCommandTag(t);
        p.addCommandTag(STAGE+n);
    }
    private void saveOrigin(ServerPlayerEntity p,BlockPos o) {
        remove(p,X); remove(p,Y); remove(p,Z);
        p.addCommandTag(X+o.getX()); p.addCommandTag(Y+o.getY()); p.addCommandTag(Z+o.getZ());
    }
    private BlockPos origin(ServerPlayerEntity p) {
        return new BlockPos(read(p,X,p.getBlockX()),read(p,Y,p.getBlockY()-1),read(p,Z,p.getBlockZ()));
    }
    private int read(ServerPlayerEntity p,String prefix,int fallback) {
        for(String t:p.getCommandTags()) if(t.startsWith(prefix)) try { return Integer.parseInt(t.substring(prefix.length())); } catch(NumberFormatException ignored) { return fallback; }
        return fallback;
    }
    private void remove(ServerPlayerEntity p,String prefix) {
        for(String t:p.getCommandTags().toArray(String[]::new)) if(t.startsWith(prefix)) p.removeCommandTag(t);
    }
    private void clear(ServerPlayerEntity p) { remove(p,STAGE); remove(p,X); remove(p,Y); remove(p,Z); }
    private boolean near(BlockPos a,BlockPos b) { return a.getSquaredDistance(b)<=4.0; }
    private void say(ServerPlayerEntity p,String s) { p.sendMessage(Text.literal(s),false); }
}
