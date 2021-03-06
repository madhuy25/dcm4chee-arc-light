import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {FormsModule, FormGroup, FormGroupName, ReactiveFormsModule} from '@angular/forms';
import { HttpModule } from '@angular/http';
import { RouterModule }   from '@angular/router';
import {AppComponent} from './app.component';
import {MaterialModule, MdDialogConfig} from "@angular/material";
import { StudiesComponent } from './studies/studies.component';
import { PageNotFoundComponent } from './page-not-found/page-not-found.component';
import { ControlComponent } from './control/control.component';
import { QueuesComponent } from './monitoring/queues/queues.component';
import {SlimLoadingBarModule} from "ng2-slim-loading-bar";
import { OrderByPipe } from './pipes/order-by.pipe';
import { GetKeyPipe } from './pipes/get-key.pipe';
import {WidgetsModule, WidgetsComponents} from "./widgets/widgets.module";
import {CommonModule, DatePipe} from "@angular/common";
import { FormatDAPipe } from './pipes/format-da.pipe';
import { FormatTMPipe } from './pipes/format-tm.pipe';
import { FormatTagPipe } from './pipes/format-tag.pipe';
import { ContentDescriptionPipe } from './pipes/content-description.pipe';
import { FormatAttributeValuePipe } from './pipes/format-attribute-value.pipe';
import { AttributeNameOfPipe } from './pipes/attribute-name-of.pipe';
import { RemovedotsPipe } from './pipes/removedots.pipe';
import {StudiesService} from "./studies/studies.service";
import {AppService} from "./app.service";
import { AttributeListComponent } from './helpers/attribute-list/attribute-list.component';
import { FileAttributeListComponent } from './helpers/file-attribute-list/file-attribute-list.component';
import {CalendarModule} from "primeng/components/calendar/calendar";
import {SelectItem} from "primeng/components/common/api";
import {DropdownModule} from "primeng/components/dropdown/dropdown";
import { TrimPipe } from './pipes/trim.pipe';
import { SearchPipe } from './pipes/search.pipe';
import { KeysPipe } from './pipes/keys.pipe';
import { IodFormGeneratorComponent } from './helpers/iod-form-generator/iod-form-generator.component';
import { TooltipDirective } from './helpers/tooltip/tooltip.directive';
import { ComparewithiodPipe } from './pipes/comparewithiod.pipe';
import { PlaceholderchangerDirective } from './helpers/placeholderchanger.directive';
import {QueuesService} from "./monitoring/queues/queues.service";
import { DevicesComponent } from './devices/devices.component';
import {DevicesService} from "./devices/devices.service";
import { DeviceConfiguratorComponent } from './device-configurator/device-configurator.component';
import {FormElement} from "./helpers/form/form-element";
import {FormService} from "./helpers/form/form.service";
import {ArrayElement} from "./helpers/form/array-element";
import {ArrayObject} from "./helpers/form/array-object";
import {DropdownList} from "./helpers/form/dropdown-list";
import {InputText} from "./helpers/form/input-text";
// import {REACTIVE_FORM_DIRECTIVES} from "@angular/forms/src/directives";
import {DynamicFormElementComponent} from "./widgets/dynamicform/dynamic-form-element.component";
import {DynamicFormComponent} from "./widgets/dynamicform/dynamic-form.component";
import { ExportComponent } from './monitoring/export/export.component';
import {ExportService} from "./monitoring/export/export.service";
import { DicomConnectionFormaterPipe } from './pipes/dicom-connection-formater.pipe';
import { AssociationsComponent } from './monitoring/associations/associations.component';
import { StorageCommitmentComponent } from './monitoring/storage-commitment/storage-commitment.component';
import {StorageCommitmentService} from "./monitoring/storage-commitment/storage-commitment.service";
import { ConnectionFormaterComponent } from './helpers/connection-formater/connection-formater.component';
import { AeListComponent } from './ae-list/ae-list.component';
import {CreateExporterService} from "./widgets/dialogs/create-exporter/create-exporter.service";
import {DeviceConfiguratorService} from "./device-configurator/device-configurator.service";
import { UtcPipe } from './pipes/utc.pipe';
import { CustomValidatorDirective } from './helpers/custom-validator/custom-validator.directive';
import {BrowserAnimationsModule, NoopAnimationsModule} from "@angular/platform-browser/animations";

@NgModule({
    declarations: [
        AppComponent,
        StudiesComponent,
        PageNotFoundComponent,
        ControlComponent,
        QueuesComponent,
        OrderByPipe,
        GetKeyPipe,
        WidgetsComponents,
        FormatDAPipe,
        FormatTMPipe,
        FormatTagPipe,
        ContentDescriptionPipe,
        FormatAttributeValuePipe,
        AttributeNameOfPipe,
        RemovedotsPipe,
        AttributeListComponent,
        FileAttributeListComponent,
        TrimPipe,
        SearchPipe,
        KeysPipe,
        IodFormGeneratorComponent,
        TooltipDirective,
        ComparewithiodPipe,
        PlaceholderchangerDirective,
        DevicesComponent,
        DeviceConfiguratorComponent,
        DynamicFormElementComponent,
        DynamicFormComponent,
        ExportComponent,
        DicomConnectionFormaterPipe,
        AssociationsComponent,
        StorageCommitmentComponent,
        ConnectionFormaterComponent,
        AeListComponent,
        UtcPipe,
        CustomValidatorDirective
    ],
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule,
        MaterialModule.forRoot(),
        SlimLoadingBarModule.forRoot(),
        WidgetsModule,
        CommonModule,
        CalendarModule,
        DropdownModule,
        ReactiveFormsModule,
        BrowserAnimationsModule,
        RouterModule.forRoot([
            {
              path: '',
              redirectTo: '/studies',
              pathMatch: 'full'
            },
            {
                path: 'monitoring',
                redirectTo: '/monitoring/queues',
                pathMatch: 'full'
            },
            { path: 'studies', component: StudiesComponent },
            { path: 'control', component: ControlComponent },
            { path: 'monitoring/export', component: ExportComponent },
            { path: 'monitoring/queues', component: QueuesComponent },
            { path: 'monitoring/associations', component: AssociationsComponent },
            { path: 'monitoring/storage-commitment', component: StorageCommitmentComponent },
            { path: 'device/devicelist', component: DevicesComponent },
            { path: 'device/aelist', component: AeListComponent },
            { path: 'device/edit/:device', component: DeviceConfiguratorComponent },
            { path: 'device/edit/:device/:devicereff', component: DeviceConfiguratorComponent },
            { path: 'device/edit/:device/:devicereff/:schema', component: DeviceConfiguratorComponent },
            { path: '**', component: PageNotFoundComponent }
      ],
            { useHash: true })
    ],
    entryComponents:[WidgetsComponents],
    providers: [
        MdDialogConfig,
        WidgetsComponents,
        AppService,
        StudiesService,
        QueuesService,
        DevicesService,
        ExportService,
        DatePipe,
        CalendarModule,
        DropdownModule,
        StorageCommitmentService,
        CreateExporterService,
        DeviceConfiguratorService
    ],
    bootstrap: [AppComponent]
})
export class AppModule { }
