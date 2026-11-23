package ru.lastsignal.client;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import ru.lastsignal.LastSignalContent;
import ru.lastsignal.OrlovEntity;
import ru.lastsignal.GuardEntity;

public final class LastSignalClient implements ClientModInitializer {
 @Override public void onInitializeClient(){
  EntityRendererRegistry.register(LastSignalContent.ORLOV,OrlovRenderer::new);
  EntityRendererRegistry.register(LastSignalContent.GUARD,GuardRenderer::new);
 }
 private static final class HumanoidModel<T extends LivingEntity> extends SinglePartEntityModel<T>{
  private final ModelPart root,head,body,leftArm,rightArm,leftLeg,rightLeg;
  HumanoidModel(ModelPart root){this.root=root;head=root.getChild("head");body=root.getChild("body");leftArm=root.getChild("left_arm");rightArm=root.getChild("right_arm");leftLeg=root.getChild("left_leg");rightLeg=root.getChild("right_leg");}
  static TexturedModelData data(boolean guard){
   ModelData d=new ModelData();ModelPartData r=d.getRoot();
   ModelPartData h=r.addChild("head",ModelPartBuilder.create().uv(0,0).cuboid(-4,-8,-4,8,8,8),ModelTransform.pivot(0,0,0));
   if(guard){h.addChild("helmet",ModelPartBuilder.create().uv(32,0).cuboid(-4.5f,-9,-4.5f,9,4,9),ModelTransform.NONE);h.addChild("visor",ModelPartBuilder.create().uv(32,14).cuboid(-3.5f,-5.5f,-4.65f,7,2,1),ModelTransform.NONE);h.addChild("mask",ModelPartBuilder.create().uv(48,14).cuboid(-2.5f,-3.5f,-4.55f,5,3,1),ModelTransform.NONE);}
   else{h.addChild("hair",ModelPartBuilder.create().uv(32,0).cuboid(-4.2f,-8.5f,-4.2f,8.4f,2.5f,8.4f),ModelTransform.NONE);h.addChild("earpiece",ModelPartBuilder.create().uv(48,0).cuboid(3.8f,-3,-1,1,2,2),ModelTransform.NONE);}
   ModelPartData b=r.addChild("body",ModelPartBuilder.create().uv(16,16).cuboid(-4,0,-2,8,12,4),ModelTransform.pivot(0,0,0));
   if(guard){b.addChild("vest",ModelPartBuilder.create().uv(0,32).cuboid(-4.2f,1,-2.5f,8.4f,7,5),ModelTransform.NONE);b.addChild("pack",ModelPartBuilder.create().uv(32,32).cuboid(-3,2,2,6,7,3),ModelTransform.NONE);b.addChild("belt",ModelPartBuilder.create().uv(0,46).cuboid(-4.1f,8,-2.2f,8.2f,1.5f,4.4f),ModelTransform.NONE);}
   else{b.addChild("coat",ModelPartBuilder.create().uv(0,32).cuboid(-4.15f,1,-2.3f,8.3f,10,.6f),ModelTransform.NONE);b.addChild("lanyard",ModelPartBuilder.create().uv(32,46).cuboid(-.5f,0,-2.45f,1,5,.4f),ModelTransform.NONE);b.addChild("pocket",ModelPartBuilder.create().uv(40,46).cuboid(1.5f,5,-2.65f,2,2,.5f),ModelTransform.NONE);}
   r.addChild("left_arm",ModelPartBuilder.create().uv(32,16).cuboid(-1,-2,-2,4,12,4),ModelTransform.pivot(5,2,0));
   r.addChild("right_arm",ModelPartBuilder.create().uv(32,16).mirrored().cuboid(-3,-2,-2,4,12,4),ModelTransform.pivot(-5,2,0));
   r.addChild("left_leg",ModelPartBuilder.create().uv(16,32).cuboid(-2,0,-2,4,12,4),ModelTransform.pivot(2,12,0));
   r.addChild("right_leg",ModelPartBuilder.create().uv(16,32).mirrored().cuboid(-2,0,-2,4,12,4),ModelTransform.pivot(-2,12,0));
   return TexturedModelData.of(d,64,64);
  }
  @Override public ModelPart getPart(){return root;}
  @Override public void setAngles(T e,float a,float d,float tick,float yaw,float pitch){head.yaw=yaw*((float)Math.PI/180F);head.pitch=pitch*((float)Math.PI/180F);rightArm.pitch=(float)Math.cos(a*.6662F+Math.PI)*d;leftArm.pitch=(float)Math.cos(a*.6662F)*d;rightLeg.pitch=(float)Math.cos(a*.6662F)*1.4F*d;leftLeg.pitch=(float)Math.cos(a*.6662F+Math.PI)*1.4F*d;body.yaw=0;}
 }
 private static final class OrlovRenderer extends MobEntityRenderer<OrlovEntity,HumanoidModel<OrlovEntity>>{
  private static final Identifier TEX=Identifier.of("lastsignal","textures/entity/orlov.png");
  OrlovRenderer(EntityRendererFactory.Context c){super(c,new HumanoidModel<>(HumanoidModel.data(false).createModel()),.45f);}
  @Override public Identifier getTexture(OrlovEntity e){return TEX;}
 }
 private static final class GuardRenderer extends MobEntityRenderer<GuardEntity,HumanoidModel<GuardEntity>>{
  private static final Identifier TEX=Identifier.of("lastsignal","textures/entity/guard.png");
  GuardRenderer(EntityRendererFactory.Context c){super(c,new HumanoidModel<>(HumanoidModel.data(true).createModel()),.5f);}
  @Override public Identifier getTexture(GuardEntity e){return TEX;}
 }
}