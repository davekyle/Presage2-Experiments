Ext.require([
	'Presage2.view.RoadVisualiser'
]);

Ext.define('Presage2.view.RoadPlugin', {
	extend: 'Presage2.view.VisualiserPlugin',
	alias: 'widget.roadplugin',
	initComponent: function() {
		Ext.apply(this, {
			drawPanel: 'Presage2.view.RoadVisualiser'
		});
		this.callParent(arguments);
	}
});
