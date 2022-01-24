import java.util.*;
import java.util.logging.Logger;

nodesToDetach = message.payload.selectNodes("(/DTOApplication/DTOLine/DTOCoverage[@CoverageCd=\'CEL\'])[1]/DTOSteps");
for (Node node : nodesToDetach){
	node.detach();
}

nodesToDetach = message.payload.getRootElement().selectNodes("(/DTOApplication/DTOLine/DTOCoverage[@CoverageCd=\'CEL\'])[1]/DTORateArea");
for (Node node : nodesToDetach){
	node.detach();
}

nodesToDetach = message.payload.selectNodes("(/DTOApplication/DTOLine/DTOCoverage[@CoverageCd=\'TRIA\'])[1]/DTOSteps");
for (Node node : nodesToDetach){
	node.detach();
}

nodesToDetach = message.payload.getRootElement().selectNodes("(/DTOApplication/DTOLine/DTOCoverage[@CoverageCd=\'TRIA\'])[1]/DTORateArea");
for (Node node : nodesToDetach){
	node.detach();
}

nodeLine = message.payload.selectSingleNode("(/DTOApplication/DTOLine[@LineCd=\'CommercialExcess\'])[1]");
nodeLine.addAttribute("FullTermAmt",flowVars.final_premium);
nodeLine.addAttribute("FinalPremiumAmt",flowVars.final_premium);
nodeLine.addAttribute("WrittenPremiumAmt",flowVars.final_premium);
nodeLine.addAttribute("RatingService","DRC");

nodeCEL = message.payload.selectSingleNode("(/DTOApplication/DTOLine/DTOCoverage[@CoverageCd=\'CEL\'])[1]");
nodeCEL.addAttribute("FullTermAmt",flowVars.xl_premium);
nodeCEL.addAttribute("FinalPremiumAmt",flowVars.xl_premium);
nodeCEL.addAttribute("WrittenPremiumAmt",flowVars.xl_premium);

nodes = domDRC.getRootElement().selectNodes("/Envelope/Body/rateResponse/RateResult/Layers/Layer","LayerNum");  

if (flowVars.tria_option == 'Yes') {

	nodeTRIA = message.payload.selectSingleNode("(/DTOApplication/DTOLine/DTOCoverage[@CoverageCd=\'TRIA\'])[1]");
	nodeTRIA.addAttribute("FullTermAmt",flowVars.tria_premium);
	nodeTRIA.addAttribute("FinalPremiumAmt",flowVars.tria_premium);
	nodeTRIA.addAttribute("WrittenPremiumAmt",flowVars.tria_premium);

	nodeTRIA.addElement("DTOSteps");
	nodeTRIASteps = nodeTRIA.selectSingleNode("DTOSteps");

	for (Node node : nodes) {    
		if (node.valueOf("InclFlag") == 'Y') {
			
			triaStepsStep = nodeTRIASteps.addElement("DTOStep")
				.addAttribute("Order",node.valueOf("Desc").replace("M",""))
				.addAttribute("Name","Rate Area: "+node.valueOf("Desc") + " TRIA")
				.addAttribute("Desc",node.valueOf("Desc")+" TRIA Layer Premium")
				.addAttribute("Operation","+")
				.addAttribute("Factor",node.valueOf("TRIACharge"))
				.addAttribute("Value",node.valueOf("TRIACharge"));

			triaRateArea = nodeTRIA.addElement("DTORateArea")
				.addAttribute("AreaName",node.valueOf("Desc") + " TRIA")
				.addAttribute("Description",node.valueOf("Desc")+" TRIA Layer")
				.addAttribute("FullTermAmt",node.valueOf("TRIACharge"))
				.addAttribute("FinalPremiumAmt",node.valueOf("TRIACharge"))
				.addAttribute("WrittenPremiumAmt",node.valueOf("TRIACharge"));	

			triaRateAreaSteps = triaRateArea.addElement("DTOSteps");
		
			triaRateAreaStepsStep1 = triaRateAreaSteps.addElement("DTOStep")
				.addAttribute("Order","1")
				.addAttribute("Name",node.valueOf("Desc") + " TRIA")
				.addAttribute("Desc",node.valueOf("Desc") + " TRIA Premium")
				.addAttribute("Operation","=")
				.addAttribute("Factor",node.valueOf("TRIACharge"))
				.addAttribute("Value",node.valueOf("TRIACharge"));
	
		}
	}
}

nodeCEL.addElement("DTOSteps");
celSteps = nodeCEL.selectSingleNode("DTOSteps");

firstMillionPrem = domDRC.getRootElement().selectSingleNode("/Envelope/Body/rateResponse/RateResult/FirstLayer").valueOf("FinalPremium");
autoFactor =  domDRC.getRootElement().selectSingleNode("/Envelope/Body/rateResponse/RateResult/FirstLayer").valueOf("AutoFactor");
nonAutoFactor =  domDRC.getRootElement().selectSingleNode("/Envelope/Body/rateResponse/RateResult/FirstLayer").valueOf("NonAutoFactor");
factorOverrideInd = domDRC.getRootElement().selectSingleNode("/Envelope/Body/rateResponse/RateResult/Total").valueOf("LayerFactorOverrideInd");
inputLayerFactorMod = domDRC.getRootElement().selectSingleNode("/Envelope/Body/rateResponse/RateResult/Input").valueOf("LayerFactorMod");
policyTerm = domDRC.getRootElement().selectSingleNode("/Envelope/Body/rateResponse/RateResult/Policy").valueOf("Term");
policyTermFactor = domDRC.getRootElement().selectSingleNode("/Envelope/Body/rateResponse/RateResult/Policy").valueOf("TermFactor");

int stepCounter = 1;

for (Node node : nodes) {    
	if (node.valueOf("InclFlag") == 'Y') {
		
		if ( (int) node.valueOf("LayerPremNonAuto") > 0) {

			stepCounter = 1;

			celStepsStep = celSteps.addElement("DTOStep")
				.addAttribute("Order",node.valueOf("Desc").replace("M",""))
				.addAttribute("Name","Rate Area: "+node.valueOf("Desc") + " NonAuto")
				.addAttribute("Desc",node.valueOf("Desc")+" NonAuto Layer Premium")
				.addAttribute("Operation","+")
				.addAttribute("Factor",node.valueOf("LayerPremNonAuto"))
				.addAttribute("Value",node.valueOf("LayerPremNonAuto"));

			celRateArea = nodeCEL.addElement("DTORateArea")
				.addAttribute("AreaName",node.valueOf("Desc") + " NonAuto")
				.addAttribute("Description",node.valueOf("Desc")+" NonAuto Layer")
				.addAttribute("FullTermAmt",node.valueOf("LayerPremNonAuto"))
				.addAttribute("FinalPremiumAmt",node.valueOf("LayerPremNonAuto"))
				.addAttribute("WrittenPremiumAmt",node.valueOf("LayerPremNonAuto"));
		
			celRateAreaSteps = celRateArea.addElement("DTOSteps");
		
			celRateAreaStepsStep1 = celRateAreaSteps.addElement("DTOStep")
				.addAttribute("Order",String.valueOf(stepCounter))
				.addAttribute("Name","1M Premium")
				.addAttribute("Desc","1M Final Premium")
				.addAttribute("Operation","=")
				.addAttribute("Factor",firstMillionPrem)
				.addAttribute("Value",firstMillionPrem);

			stepCounter += 1;

			celRateAreaStepsStep2 = celRateAreaSteps.addElement("DTOStep")
				.addAttribute("Order",String.valueOf(stepCounter))
				.addAttribute("Name","Final Layer Factor")
				.addAttribute("Desc","Final Layer Factor")
				.addAttribute("Operation","x")
				.addAttribute("Factor",node.valueOf("FinalFactor"))
				.addAttribute("Value",node.valueOf("AnnualCalcPrem"));

			stepCounter += 1;

			if (node.valueOf("Desc") != "1M") {

				celRateAreaStepsStep2Steps = celRateAreaStepsStep2.addElement("DTOSteps");

				celRateAreaStepsStep2a = celRateAreaStepsStep2Steps.addElement("DTOStep")
					.addAttribute("Order","1")
					.addAttribute("Name","Default Layer Factor")
					.addAttribute("Desc","Default Layer Factor")
					.addAttribute("Operation","")
					.addAttribute("Factor",node.valueOf("Factor"))
					.addAttribute("Value","");

				celRateAreaStepsStep2b = celRateAreaStepsStep2Steps.addElement("DTOStep")
					.addAttribute("Order","2")
					.addAttribute("Name","Layer Modification Factor")
					.addAttribute("Desc","Layer Modification Factor")
					.addAttribute("Operation","x")
					.addAttribute("Factor",inputLayerFactorMod)
					.addAttribute("Value",node.valueOf("OverrideFactor"));
				
				celRateAreaStepsStep2c = celRateAreaStepsStep2Steps.addElement("DTOStep")
					.addAttribute("Order","3")
					.addAttribute("Name","Min Layer Factor")
					.addAttribute("Desc","Min Layer Factor")
					.addAttribute("Operation","")
					.addAttribute("Factor",node.valueOf("MinFactor"))
					.addAttribute("Value","");

				celRateAreaStepsStep2d = celRateAreaStepsStep2Steps.addElement("DTOStep")
					.addAttribute("Order","4")
					.addAttribute("Name","Max Layer Factor")
					.addAttribute("Desc","Max Layer Factor")
					.addAttribute("Operation","")
					.addAttribute("Factor",node.valueOf("MaxFactor"))
					.addAttribute("Value","");

				celRateAreaStepsStep2e = celRateAreaStepsStep2Steps.addElement("DTOStep")
					.addAttribute("Order","5")
					.addAttribute("Name","Revised Layer Factor")
					.addAttribute("Desc","Revised Layer Factor")
					.addAttribute("Operation","=")
					.addAttribute("Factor",node.valueOf("FinalFactor"))
					.addAttribute("Value",node.valueOf("FinalFactor"));

	        }

			if (policyTerm == "Short") {
				
				celRateAreaStepsStep2 = celRateAreaSteps.addElement("DTOStep")
					.addAttribute("Order",String.valueOf(stepCounter))
					.addAttribute("Name","Term Factor")
					.addAttribute("Desc","Policy Term Factor")
					.addAttribute("Operation","x")
					.addAttribute("Factor",policyTermFactor)
					.addAttribute("Value",node.valueOf("CalcPrem"));

				stepCounter += 1;
			
			}
	
			celRateAreaStepsStep3 = celRateAreaSteps.addElement("DTOStep")
				.addAttribute("Order",String.valueOf(stepCounter))
				.addAttribute("Name","Layer Minimum")
				.addAttribute("Desc",node.valueOf("Desc")+" Layer Minimum")
				.addAttribute("Operation","")
				.addAttribute("Factor",node.valueOf("MinPrem"))
				.addAttribute("Value","");

			stepCounter += 1;
			
			int balanceToMin = 0;
			if ((int) node.valueOf("MinPrem") > (int) node.valueOf("CalcPrem")){
				balanceToMin = (int) node.valueOf("MinPrem") - (int) node.valueOf("CalcPrem")
			}
			
			celRateAreaStepsStep4 = celRateAreaSteps.addElement("DTOStep")
				.addAttribute("Order",String.valueOf(stepCounter))
				.addAttribute("Name","Balance To Minimum")
				.addAttribute("Desc","Balance To Layer Minimum")
				.addAttribute("Operation","+")
				.addAttribute("Factor",balanceToMin)
				.addAttribute("Value",node.valueOf("LayerPrem"));

			stepCounter += 1;

			celRateAreaStepsStep5 = celRateAreaSteps.addElement("DTOStep")
				.addAttribute("Order",String.valueOf(stepCounter))
				.addAttribute("Name","NonAuto Ratio Factor")
				.addAttribute("Desc","NonAuto Ratio Factor")
				.addAttribute("Operation","x")
				.addAttribute("Factor",nonAutoFactor)
				.addAttribute("Value",node.valueOf("LayerPremNonAuto"));

		}

		if ( (int) node.valueOf("LayerPremAuto") > 0) {

			stepCounter = 1;

			celStepsStep = celSteps.addElement("DTOStep")
				.addAttribute("Order",node.valueOf("Desc").replace("M",""))
				.addAttribute("Name","Rate Area: "+node.valueOf("Desc") + " Auto")
				.addAttribute("Desc",node.valueOf("Desc")+" Auto Layer Premium")
				.addAttribute("Operation","+")
				.addAttribute("Factor",node.valueOf("LayerPremAuto"))
				.addAttribute("Value",node.valueOf("LayerPremAuto"));

			celRateArea = nodeCEL.addElement("DTORateArea")
				.addAttribute("AreaName",node.valueOf("Desc") + " Auto")
				.addAttribute("Description",node.valueOf("Desc")+" Auto Layer")
				.addAttribute("FullTermAmt",node.valueOf("LayerPremAuto"))
				.addAttribute("FinalPremiumAmt",node.valueOf("LayerPremAuto"))
				.addAttribute("WrittenPremiumAmt",node.valueOf("LayerPremAuto"));
		
			celRateAreaSteps = celRateArea.addElement("DTOSteps");
		
			celRateAreaStepsStep1 = celRateAreaSteps.addElement("DTOStep")
				.addAttribute("Order",String.valueOf(stepCounter))
				.addAttribute("Name","1M Premium")
				.addAttribute("Desc","1M Final Premium")
				.addAttribute("Operation","=")
				.addAttribute("Factor",firstMillionPrem)
				.addAttribute("Value",firstMillionPrem);

			stepCounter += 1;
			celRateAreaStepsStep2 = celRateAreaSteps.addElement("DTOStep")
				.addAttribute("Order",String.valueOf(stepCounter))
				.addAttribute("Name","Final Layer Factor")
				.addAttribute("Desc","Final Layer Factor")
				.addAttribute("Operation","x")
				.addAttribute("Factor",node.valueOf("FinalFactor"))
				.addAttribute("Value",node.valueOf("CalcPrem"));

			stepCounter += 1;

			if (node.valueOf("Desc") != "1M") {

				celRateAreaStepsStep2Steps = celRateAreaStepsStep2.addElement("DTOSteps");

				celRateAreaStepsStep2a = celRateAreaStepsStep2Steps.addElement("DTOStep")
					.addAttribute("Order","1")
					.addAttribute("Name","Default Layer Factor")
					.addAttribute("Desc","Default Layer Factor")
					.addAttribute("Operation","")
					.addAttribute("Factor",node.valueOf("Factor"))
					.addAttribute("Value","");

				celRateAreaStepsStep2b = celRateAreaStepsStep2Steps.addElement("DTOStep")
					.addAttribute("Order","2")
					.addAttribute("Name","Layer Modification Factor")
					.addAttribute("Desc","Layer Modification Factor")
					.addAttribute("Operation","x")
					.addAttribute("Factor",inputLayerFactorMod)
					.addAttribute("Value",node.valueOf("OverrideFactor"));
				
				celRateAreaStepsStep2c = celRateAreaStepsStep2Steps.addElement("DTOStep")
					.addAttribute("Order","3")
					.addAttribute("Name","Min Layer Factor")
					.addAttribute("Desc","Min Layer Factor")
					.addAttribute("Operation","")
					.addAttribute("Factor",node.valueOf("MinFactor"))
					.addAttribute("Value","");

				celRateAreaStepsStep2d = celRateAreaStepsStep2Steps.addElement("DTOStep")
					.addAttribute("Order","4")
					.addAttribute("Name","Max Layer Factor")
					.addAttribute("Desc","Max Layer Factor")
					.addAttribute("Operation","")
					.addAttribute("Factor",node.valueOf("MaxFactor"))
					.addAttribute("Value","");

				celRateAreaStepsStep2e = celRateAreaStepsStep2Steps.addElement("DTOStep")
					.addAttribute("Order","5")
					.addAttribute("Name","Revised Layer Factor")
					.addAttribute("Desc","Revised Layer Factor")
					.addAttribute("Operation","=")
					.addAttribute("Factor",node.valueOf("FinalFactor"))
					.addAttribute("Value",node.valueOf("FinalFactor"));

	        }
					
			celRateAreaStepsStep3 = celRateAreaSteps.addElement("DTOStep")
				.addAttribute("Order",String.valueOf(stepCounter))
				.addAttribute("Name","Layer Minimum")
				.addAttribute("Desc",node.valueOf("Desc")+" Layer Minimum")
				.addAttribute("Operation","")
				.addAttribute("Factor",node.valueOf("MinPrem"))
				.addAttribute("Value","");

			stepCounter += 1;
					
			int balanceToMin = 0;
			if ((int) node.valueOf("MinPrem") > (int) node.valueOf("CalcPrem")){
				balanceToMin = (int) node.valueOf("MinPrem") - (int) node.valueOf("CalcPrem")
			}
			
			celRateAreaStepsStep4 = celRateAreaSteps.addElement("DTOStep")
				.addAttribute("Order",String.valueOf(stepCounter))
				.addAttribute("Name","Balance To Minimum")
				.addAttribute("Desc","Balance To Layer Minimum")
				.addAttribute("Operation","+")
				.addAttribute("Factor",balanceToMin)
				.addAttribute("Value",node.valueOf("LayerPrem"));

			stepCounter += 1;
		
			celRateAreaStepsStep5 = celRateAreaSteps.addElement("DTOStep")
				.addAttribute("Order",String.valueOf(stepCounter))
				.addAttribute("Name","Auto Ratio Factor")
				.addAttribute("Desc","Auto Ratio Factor")
				.addAttribute("Operation","x")
				.addAttribute("Factor",autoFactor)
				.addAttribute("Value",node.valueOf("LayerPremAuto"));

		}			
	}
}
