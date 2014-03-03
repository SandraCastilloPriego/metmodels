/*
 * Copyright 2007-2012 
 *
 * This file is part of MetModels.
 *
 * MetModels is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * MetModels is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * MetModels; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package MM.main;

import MM.modules.celldesigner.CellDesignerModule;
import MM.modules.celldesignernames.CellDesignerNamesModule;
import MM.modules.database.metacyc.MetacycModule;
import MM.modules.file.openfile.OpenBasicFileModule;
import MM.modules.dataanalysis.compare_reactions.CompareReactionsModule;
import MM.modules.utils.get_ebi_ids.GetEBIIDSModule;
import MM.modules.utils.get_pubchem_ids.GetPubchemIDSModule;
import MM.modules.utils.get_pubchem_info.GetPubchemInfoModule;
import MM.modules.utils.printAllSpecies.PrintAllSpeciesModule;
import MM.modules.utils.printreactions.PrintReactionsModule;
import MM.modules.utils.printspecies.PrintSpeciesModule;
import MM.modules.dataanalysis.search_pathway.SearchPathwaysModule;
import MM.modules.dataanalysis.search_pathway2points.SearchPathwaysTwoPointsModule;
import MM.modules.dataanalysis.search_pathway2pointsFluxes.SearchPathwaysTwoPointsFluxesModule;
import MM.modules.dataanalysis.search_reaction.SearchReactionsModule;
import MM.modules.database.neo4j.addModel.AddModelModule;
import MM.modules.database.neo4j.modelRelationships.CombineModelModule;
import MM.modules.reconstruction.gapfilling.GapFillingModule;

/**
 * List of modules included in MM
 */
public class MMModulesList {

        public static final Class<?> MODULES[] = new Class<?>[]{
              OpenBasicFileModule.class,
              CompareReactionsModule.class,
              SearchReactionsModule.class,
              GetEBIIDSModule.class,
              GetPubchemIDSModule.class,
              SearchPathwaysModule.class,
              SearchPathwaysTwoPointsModule.class,
              PrintReactionsModule.class,
              PrintSpeciesModule.class,
              MetacycModule.class,
              PrintAllSpeciesModule.class,
              GetPubchemInfoModule.class,
              CellDesignerModule.class,
              CellDesignerNamesModule.class,
              AddModelModule.class,
              CombineModelModule.class,
              SearchPathwaysTwoPointsFluxesModule.class,
              GapFillingModule.class

        };
}
